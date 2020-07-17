package com.virnect.license.application;

import com.virnect.license.application.rest.user.UserRestService;
import com.virnect.license.application.rest.workspace.WorkspaceRestService;
import com.virnect.license.dao.LicenseAssignAuthInfoRepository;
import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.dao.product.LicenseProductRepository;
import com.virnect.license.dao.product.ProductRepository;
import com.virnect.license.domain.billing.LicenseAssignAuthInfo;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.licenseplan.PlanStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;
import com.virnect.license.dto.request.billing.LicenseAllocateCheckRequest;
import com.virnect.license.dto.request.billing.LicenseAllocateProductInfoResponse;
import com.virnect.license.dto.request.billing.LicenseProductAllocateRequest;
import com.virnect.license.dto.request.billing.LicenseProductDeallocateRequest;
import com.virnect.license.dto.response.LicenseProductAllocateCheckResponse;
import com.virnect.license.dto.response.LicenseProductDeallocateResponse;
import com.virnect.license.dto.response.biling.LicenseProductAllocateResponse;
import com.virnect.license.dto.rest.UserInfoRestResponse;
import com.virnect.license.dto.rest.WorkspaceInfoListResponse;
import com.virnect.license.dto.rest.WorkspaceInfoResponse;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
    private final ProductRepository productRepository;
    private final UserRestService userRestService;
    private final WorkspaceRestService workspaceRestService;
    private final LicensePlanRepository licensePlanRepository;
    private final LicenseProductRepository licenseProductRepository;
    private final LicenseRepository licenseRepository;
    private final LicenseAssignAuthInfoRepository licenseAssignAuthInfoRepository;

    private static long MAX_USER_AMOUNT = 9; // 9 명
    private static long MAX_CALL_TIME = 270; // 270 시간
    private static long MAX_STORAGE_AMOUNT = 90000; // 90 기가
    private static long MAX_DOWNLOAD_HITS = 1000000; // 10만 회
    private static int LICENSE_EXPIRED_HOUR = 23; // 오후 11시
    private static int LICENSE_EXPIRED_MINUTE = 59; // 59분
    private static int LICENSE_EXPIRED_SECONDS = 59; // 59초

    private static long LICENSE_ASSIGN_AUTH_CODE_TTL_MINUTE = 30; // 30분간 지급 인증 코드 유효

    /**
     * 상품 지급 여부 검사
     *
     * @param allocateCheckRequest
     * @return
     */
    @Transactional
    public ApiResponse<LicenseProductAllocateCheckResponse> licenseAllocateCheckRequest(LicenseAllocateCheckRequest allocateCheckRequest) {
        log.info("[BILLING][LICENSE ALLOCATE CHECK] -> [{}]", allocateCheckRequest.toString());

        // 1. 라이선스 지급 여부 검사 요청 사용자 정보 조회
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(allocateCheckRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData() == null) {
            log.error("User service error response: [{}]", userInfoApiResponse.getMessage());
            throw new BillingServiceException(ErrorCode.ERR_BILLING_LICENSE_SERVER_ERROR);
        }

        UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

        // 2. 사용자의 현재 사용중인 라이선스 플랜 조회
        LicensePlan licensePlan = licensePlanRepository.findByUserIdAndPlanStatus(requestUserInfo.getUuid(), PlanStatus.ACTIVE);
        long calculateMaxCallTime = 0;
        long calculateMaxStorage = 0;
        long calculateMaxHit = 0;

        // 3. 현재 사용 중인 라이선스 플랜 정보가 있는 경우
        if (licensePlan != null) {
            // 2.1  기존 라이선스 플랜의 서비스 이용 정보 추가
            calculateMaxCallTime += licensePlan.getMaxCallTime();
            calculateMaxStorage += licensePlan.getMaxStorageSize();
            calculateMaxHit += licensePlan.getMaxDownloadHit();
        }

        // 4. 상품 주문 정보 추가 및 구매 정보 검사 (정기 결제가 아닌건에 대해서만 검증)
        if (!allocateCheckRequest.isRegularRequest()) {
            calculateMaxCallTime += allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductCallTime).sum();
            calculateMaxStorage += allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductStorage).sum();
            calculateMaxHit += allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductHit).sum();

            // 5. 최대 통화 수 , 최대 용량, 최대 다운로드 횟수 비교
            if (calculateMaxCallTime > MAX_CALL_TIME || calculateMaxStorage > MAX_STORAGE_AMOUNT || calculateMaxHit > MAX_DOWNLOAD_HITS) {
                throw new LicenseAllocateDeniedException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED, allocateCheckRequest.getUserId());
            } else {
                log.info("USER : [{}] -> CALCULATE CALL TIME : [{}] , CALCULATE STORAGE: [{}] , CALCULATE DOWNLOAD: [{}]", allocateCheckRequest.getUserId(), calculateMaxCallTime, calculateMaxStorage, calculateMaxHit);
            }
        }

        // 6. 지급 인증 정보 생성
        String assignAuthCoe = UUID.randomUUID().toString();
        LocalDateTime assignDate = LocalDateTime.now();
        LicenseAssignAuthInfo licenseAssignAuthInfo = new LicenseAssignAuthInfo();
        licenseAssignAuthInfo.setAssignAuthCode(assignAuthCoe);
        licenseAssignAuthInfo.setUserId(allocateCheckRequest.getUserId());
        licenseAssignAuthInfo.setUuid(requestUserInfo.getUuid());
        licenseAssignAuthInfo.setUserName(requestUserInfo.getName());
        licenseAssignAuthInfo.setEmail(requestUserInfo.getEmail());
        licenseAssignAuthInfo.setAssignableCheckDate(assignDate);
        licenseAssignAuthInfo.setTotalProductCallTime(allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductCallTime).sum());
        licenseAssignAuthInfo.setTotalProductHit(allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductHit).sum());
        licenseAssignAuthInfo.setTotalProductStorage(allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductStorage).sum());
        licenseAssignAuthInfo.setExpiredDate(Duration.ofMinutes(LICENSE_ASSIGN_AUTH_CODE_TTL_MINUTE).getSeconds());
        licenseAssignAuthInfo.setRegularRequest(allocateCheckRequest.isRegularRequest());

        // 7. 지급 인증 정보 저장
        licenseAssignAuthInfoRepository.save(licenseAssignAuthInfo);

        // 8. 지급 여부 결과 정보 생성
        LicenseProductAllocateCheckResponse checkResponse = new LicenseProductAllocateCheckResponse();
        checkResponse.setAssignable(true);
        checkResponse.setAssignAuthCode(assignAuthCoe);
        checkResponse.setUserId(allocateCheckRequest.getUserId());
        checkResponse.setAssignableCheckDate(LocalDateTime.now());
        return new ApiResponse<>(checkResponse);
    }


    /**
     * 상품 지급
     *
     * @param licenseAllocateRequest
     * @return
     */
    @Transactional
    public ApiResponse<LicenseProductAllocateResponse> licenseAllocateRequest(LicenseProductAllocateRequest licenseAllocateRequest) {
        // 1. 상품 지급 인증 정보 조회
        LicenseAssignAuthInfo licenseAssignAuthInfo = licenseAssignAuthInfoRepository.findById(licenseAllocateRequest.getAssignAuthCode())
                .orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE));

        log.info("[FOUND ASSIGNMENT AUTH INFO]: {}", licenseAssignAuthInfo.toString());

        // 2. 지급 요청 사용자 정보 조회
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(licenseAllocateRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData() == null) {
            log.error("[USER REST SERVICE ERROR RESPONSE]: [{}]", userInfoApiResponse.getMessage());
            throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
        }

        UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

        // 4. 상품 지급 인증 정보 검증
        licenseAssignAuthInfoValidation(licenseAllocateRequest, licenseAssignAuthInfo, requestUserInfo);

        // 5. 지급 요청 사용자, 워크스페이스 정보 조회
        ApiResponse<WorkspaceInfoListResponse> workspaceApiResponse = this.workspaceRestService.getMyWorkspaceInfoList(requestUserInfo.getUuid(), 50);
        if (workspaceApiResponse.getCode() != 200 || workspaceApiResponse.getData().getWorkspaceList() == null) {
            log.error("User service error response: [{}]", workspaceApiResponse.getMessage());
            throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
        }

        // 6. 마스터 워크스페이스 정보 추출
        WorkspaceInfoResponse workspaceInfo = workspaceApiResponse.getData().getWorkspaceList()
                .stream()
                .filter(w -> w.getRole().equals("MASTER")).findFirst()
                .orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED));

        // 7. 라이선스 플랜 정보 조회
        Optional<LicensePlan> userLicensePlan = licensePlanRepository.findByUserIdAndWorkspaceIdAndPlanStatus(requestUserInfo.getUuid(), workspaceInfo.getUuid(), PlanStatus.ACTIVE);

        // 8. 지급 상품 서비스 이용 정보 계산 ( 통화 시간, 용량, 다운로드 횟수 )
        Long calculateMaxCallTime = licenseAllocateRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductCallTime).sum();
        Long calculateMaxStorage = licenseAllocateRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductStorage).sum();
        Long calculateMaxHit = licenseAllocateRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductHit).sum();

        // 8.지급 인증 정보 확인 - 통화 횟수, 용량, 다운로드 횟수
        licenseAllocatePropertyValidationCheck(licenseAssignAuthInfo, calculateMaxCallTime, calculateMaxStorage, calculateMaxHit);

        //9. 기존 활성화 된 라이선스 플랜 정보가 있는 경우
        if (userLicensePlan.isPresent()) {
            LicensePlan licensePlan = userLicensePlan.get();
            LocalDate licensePlanExpireDate = licensePlan.getEndDate().toLocalDate();

            //10. 추가 결제 요청 건인 경우
            if (!licenseAllocateRequest.isRegularRequest()) {
                // 상품 지급 건에 따른 서비스 이용 정보 갱신
                licenseRegisterByProduct(licenseAllocateRequest.getProductList(), licensePlan);
                licensePlan.setMaxCallTime(licensePlan.getMaxCallTime() + calculateMaxCallTime);
                licensePlan.setMaxDownloadHit(licensePlan.getMaxDownloadHit() + calculateMaxHit);
                licensePlan.setMaxStorageSize(licensePlan.getMaxStorageSize() + calculateMaxStorage);

                // 라이선스 만료 전에 추가 구매 시, 추가 구매 일자 - 1일 기준으로 라이선스 만료 일자 변경
                // 기존 만료일이 A월 B일 일때, C일에 구매한 경우 A+1월 C-1일에 만료, (C > B)
                LocalDateTime additionalProductPurchaseDate = LocalDate.now()
                        .plusMonths(1)
                        .minusDays(1)
                        .atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);

                licensePlan.setEndDate(additionalProductPurchaseDate);
                licensePlan.setPaymentId(licenseAllocateRequest.getPaymentId());
                licensePlan.setCountryCode(licenseAllocateRequest.getUserCountryCode());
                licensePlanRepository.save(licensePlan);
            } else { // 정기 결제인 경우,
                licensePlan.setPaymentId(licenseAllocateRequest.getPaymentId());
                licensePlan.setCountryCode(licenseAllocateRequest.getUserCountryCode());

                // 다음달 결제일 자정에 만료
                LocalDateTime nextLicenseExpiredDate = licensePlanExpireDate
                        .plusMonths(1)
                        .atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);

                licensePlan.setEndDate(nextLicenseExpiredDate);
                licensePlanRepository.save(licensePlan);
            }


            // 11. 지급 상품 라이선스 생성 (정기 결제 건이 아닌 경우)
            if (!licenseAllocateRequest.isRegularRequest()) {
                licenseRegisterByProduct(licenseAllocateRequest.getProductList(), licensePlan);
            }

            LicenseProductAllocateResponse allocateResponse = new LicenseProductAllocateResponse();
            allocateResponse.setUserId(licenseAllocateRequest.getUserId());
            allocateResponse.setPaymentId(licenseAllocateRequest.getPaymentId());
            allocateResponse.setAllocatedDate(licensePlan.getUpdatedDate());
            allocateResponse.setAllocatedProductList(licenseAllocateRequest.getProductList());
            licenseAssignAuthInfoRepository.deleteById(licenseAllocateRequest.getAssignAuthCode());

            return new ApiResponse<>(allocateResponse);
        }


        // 11. 최초 구매, 라이선스 플랜 생성
        LicensePlan licensePlan = LicensePlan.builder()
                .userId(requestUserInfo.getUuid())
                .workspaceId(workspaceInfo.getUuid())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30))
                .planStatus(PlanStatus.ACTIVE)
                .maxCallTime(calculateMaxCallTime)
                .maxDownloadHit(calculateMaxHit)
                .maxStorageSize(calculateMaxStorage)
                .maxUserAmount(MAX_USER_AMOUNT)
                .paymentId(licenseAllocateRequest.getPaymentId())
                .countryCode(licenseAllocateRequest.getUserCountryCode())
                .build();
        licensePlanRepository.save(licensePlan);

        // 12. 지급 요청 상품 라이선스 생성
        licenseRegisterByProduct(licenseAllocateRequest.getProductList(), licensePlan);

        LicenseProductAllocateResponse allocateResponse = new LicenseProductAllocateResponse();
        allocateResponse.setUserId(licenseAllocateRequest.getUserId());
        allocateResponse.setPaymentId(licenseAllocateRequest.getPaymentId());
        allocateResponse.setAllocatedDate(licensePlan.getUpdatedDate());
        allocateResponse.setAllocatedProductList(licenseAllocateRequest.getProductList());

        licenseAssignAuthInfoRepository.deleteById(licenseAllocateRequest.getAssignAuthCode());

        return new ApiResponse<>(allocateResponse);
    }


    /**
     * 상품 지급 취소
     *
     * @param licenseDeallocateRequest
     * @return
     */
    @Transactional
    public ApiResponse<LicenseProductDeallocateResponse> licenseDeallocateRequest(LicenseProductDeallocateRequest licenseDeallocateRequest) {
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(licenseDeallocateRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData() == null) {
            log.error("User service error response: [{}]", userInfoApiResponse.getMessage());
            throw new BillingServiceException(ErrorCode.ERR_BILLING_LICENSE_DEALLOCATE_USER_NOT_FOUND);
        }
        // 1. 계정 정보 조회
        UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

        // 2. 라이선스 플랜 정보 조회
        LicensePlan licensePlan = licensePlanRepository.findByUserIdAndPaymentId(requestUserInfo.getUuid(), licenseDeallocateRequest.getPaymentId())
                .orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_LICENSE_DEALLOCATE_PLAN_NOT_FOUND));

        // 3. 라이선스 플랜 정보 수정 기록 및 비활성화
        licensePlan.setModifiedUser(licenseDeallocateRequest.getOperatedBy());
        licensePlan.setPlanStatus(PlanStatus.INACTIVE);
        licensePlanRepository.save(licensePlan);

        LicenseProductDeallocateResponse deallocateResponse = new LicenseProductDeallocateResponse();
        deallocateResponse.setPaymentId(licenseDeallocateRequest.getPaymentId());
        deallocateResponse.setUserId(licenseDeallocateRequest.getUserId());
        deallocateResponse.setDeallocatedDate(LocalDateTime.now());
        return new ApiResponse<>(deallocateResponse);
    }

    /**
     * 라이선스 상품 지급 인증 정보 및 지급 요청 데이터 검증
     *
     * @param licenseAllocateRequest - 라이선스 지급 요청 정보
     * @param licenseAssignAuthInfo  - 라이선스 상품 지급 인증 정보
     * @param requestUserInfo        - 지급 요청 사용자 정보
     */
    private void licenseAssignAuthInfoValidation(LicenseProductAllocateRequest licenseAllocateRequest, LicenseAssignAuthInfo licenseAssignAuthInfo, UserInfoRestResponse requestUserInfo) {
        if (!licenseAssignAuthInfo.getUserId().equals(licenseAllocateRequest.getUserId()) || !licenseAssignAuthInfo.getUuid().equals(requestUserInfo.getUuid()) || licenseAllocateRequest.isRegularRequest() != licenseAssignAuthInfo.isRegularRequest()) {
            log.error("[LICENSE PRODUCT ALLOCATE AUTHENTICATION INFO CHECK] - FAIL.");
            log.error("[AUTH_CODE_INFO] - [{}]", licenseAssignAuthInfo.toString());
            log.error("[ALLOCATE_REQUEST_INFO] - [{}]", licenseAllocateRequest.toString());
            throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE);
        }
    }

    /**
     * 지급 인증 정보 확인 - 서비스 이용 범위 계산
     *
     * @param licenseAssignAuthInfo - 지급 인증 정보
     * @param calculateMaxCallTime  - 지급 요청의 통화 시간
     * @param calculateMaxStorage   - 지급 요청의 용량 정보
     * @param calculateMaxHit       - 지급 요청의 다운로드 횟수
     */
    private void licenseAllocatePropertyValidationCheck(LicenseAssignAuthInfo licenseAssignAuthInfo, Long calculateMaxCallTime, Long calculateMaxStorage, Long calculateMaxHit) {
        if (!licenseAssignAuthInfo.getTotalProductCallTime().equals(calculateMaxCallTime) || !licenseAssignAuthInfo.getTotalProductHit().equals(calculateMaxHit) || !licenseAssignAuthInfo.getTotalProductStorage().equals(calculateMaxStorage)) {
            throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE);
        }
    }

    /**
     * 상품 정보 기반으로 등록
     *
     * @param productList
     * @param licensePlan
     */
    @Transactional
    public void licenseRegisterByProduct(List<LicenseAllocateProductInfoResponse> productList, LicensePlan licensePlan) {
        if (productList == null || productList.isEmpty()) {
            throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
        }
        productList.stream()
                .filter(p -> !p.getProductType().getId().equals("service"))
                .forEach(productInfo -> {
                    Product product = this.productRepository.findById(productInfo.getProductId())
                            .orElseThrow(() -> {
                                log.error("ASSIGN REQUEST PRODUCT NOT FOUND -> [{}] ", productInfo.toString());
                                return new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
                            });

                    LicenseProduct licenseProduct = LicenseProduct.builder()
                            .product(product)
                            .licensePlan(licensePlan)
                            .quantity(productInfo.getProductAmount())
                            .build();
                    licenseProductRepository.save(licenseProduct);

                    licenseGenerateAndRegisterByLicenseProduct(licenseProduct);
                });
    }

    /**
     * 라이선스 생성
     *
     * @param licenseProduct
     */
    @Transactional
    public void licenseGenerateAndRegisterByLicenseProduct(LicenseProduct licenseProduct) {
        for (int i = 0; i < licenseProduct.getQuantity(); i++) {
            License license = License.builder()
                    .status(LicenseStatus.UNUSE)
                    .serialKey(UUID.randomUUID().toString().toUpperCase())
                    .licenseProduct(licenseProduct)
                    .build();
            this.licenseRepository.save(license);
        }
    }
}
