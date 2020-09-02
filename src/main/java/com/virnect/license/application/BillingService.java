package com.virnect.license.application;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.rest.user.UserRestService;
import com.virnect.license.application.rest.workspace.WorkspaceRestService;
import com.virnect.license.dao.LicenseAssignAuthInfoRepository;
import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.dao.product.LicenseProductRepository;
import com.virnect.license.dao.product.ProductRepository;
import com.virnect.license.dao.product.ServiceProductRepository;
import com.virnect.license.domain.billing.LicenseAssignAuthInfo;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.licenseplan.PlanStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;
import com.virnect.license.domain.product.ServiceProduct;
import com.virnect.license.dto.ResourceCalculate;
import com.virnect.license.dto.request.billing.LicenseAllocateCheckRequest;
import com.virnect.license.dto.request.billing.LicenseAllocateProductInfoResponse;
import com.virnect.license.dto.request.billing.LicenseProductAllocateRequest;
import com.virnect.license.dto.request.billing.LicenseProductDeallocateRequest;
import com.virnect.license.dto.request.billing.ProductTypeRequest;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
	private static final long MAX_USER_AMOUNT = 9; // 9 명
	private static final long MAX_CALL_TIME = 270; // 270 시간
	private static final long MAX_STORAGE_AMOUNT = 90000; // 90 기가
	private static final long MAX_DOWNLOAD_HITS = 1000000; // 10만 회
	private static final int LICENSE_EXPIRED_HOUR = 23; // 오후 11시
	private static final int LICENSE_EXPIRED_MINUTE = 59; // 59분
	private static final int LICENSE_EXPIRED_SECONDS = 59; // 59초
	private static final long LICENSE_ASSIGN_AUTH_CODE_TTL_MINUTE = 30; // 30분간 지급 인증 코드 유효
	private static final long FIRST_WORKSPACE_DOWNLOAD_HITS = 5000; // 최초 워크스페이스 생성 시 다운로드  5000회 추가
	private static final String USER_REST_SERVICE_ERROR_LOG_FORMAT = "User service error response: [{}]";
	private final ProductRepository productRepository;
	private final UserRestService userRestService;
	private final WorkspaceRestService workspaceRestService;
	private final LicensePlanRepository licensePlanRepository;
	private final LicenseProductRepository licenseProductRepository;
	private final LicenseRepository licenseRepository;
	private final LicenseAssignAuthInfoRepository licenseAssignAuthInfoRepository;
	private final ServiceProductRepository serviceProductRepository;

	/**
	 * 상품 지급 여부 검사
	 *
	 * @param allocateCheckRequest - 상품 지급 여부 조회 요청 데이터
	 * @return - 상품 지급 여부 결과 데이터
	 */
	@Transactional
	public ApiResponse<LicenseProductAllocateCheckResponse> licenseAllocateCheckRequest(
		LicenseAllocateCheckRequest allocateCheckRequest
	) {
		log.info("[BILLING][LICENSE ALLOCATE CHECK REQUEST] -> [{}]", allocateCheckRequest.toString());
		// 1. 상품 지급 여부 검사 요청 사용자 정보 조회
		UserInfoRestResponse requestUserInfo = getUserInfoRestResponseByUserId(
			allocateCheckRequest.getUserId(), ErrorCode.ERR_BILLING_LICENSE_SERVER_ERROR
		);

		// 2. 제품별 리소스 양 계산
		ResourceCalculate requestTotalResource = calculateAllocateProductResource(
			allocateCheckRequest.getProductList()
		);

		log.info("[ALLOCATE_CHECK_REQUEST] => {} , [REQUEST_TOTAL_RESOURCE] => {}",
			allocateCheckRequest, requestTotalResource
		);

		// 3. 사용자의 현재 사용중인 라이선스 플랜 조회
		Optional<LicensePlan> licensePlan = licensePlanRepository.findByUserIdAndPlanStatus(
			requestUserInfo.getUuid(),
			PlanStatus.ACTIVE
		);

		// 4. 최대 통화 수 , 최대 용량, 최대 다운로드 횟수 비교
		allocateResourceValidation(
			requestTotalResource.getTotalCallTime(), requestTotalResource.getTotalStorageSize(),
			requestTotalResource.getTotalDownloadHit(), allocateCheckRequest.getUserId(),
			licensePlan
		);

		log.info("USER : [{}] -> CALCULATE CALL TIME : [{}] , CALCULATE STORAGE: [{}] , CALCULATE DOWNLOAD: [{}]",
			allocateCheckRequest.getUserId(), requestTotalResource.getTotalCallTime(),
			requestTotalResource.getTotalStorageSize(), requestTotalResource.getTotalDownloadHit()
		);

		// 5. 지급 인증 정보 생성
		String assignAuthCoe = UUID.randomUUID().toString();
		saveLicenseAssignAuthInfo(allocateCheckRequest, requestUserInfo, requestTotalResource, assignAuthCoe);

		// 6. 지급 여부 결과 정보 생성
		LicenseProductAllocateCheckResponse checkResponse = new LicenseProductAllocateCheckResponse();
		checkResponse.setAssignable(true);
		checkResponse.setAssignAuthCode(assignAuthCoe);
		checkResponse.setUserId(allocateCheckRequest.getUserId());
		checkResponse.setAssignableCheckDate(LocalDateTime.now());
		return new ApiResponse<>(checkResponse);
	}

	/**
	 * 라이선스 지급 인증 정보 생성
	 * @param allocateCheckRequest - 라이선스 지급 여부 검사 요청
	 * @param requestUserInfo - 라이선스 지급 요청 사용자 정보
	 * @param requestTotalResource - 라이선스 지급 제품별 리소스 정보
	 * @param assignAuthCoe - 라이선스 지급 정보 인증 키
	 */
	private void saveLicenseAssignAuthInfo(
		LicenseAllocateCheckRequest allocateCheckRequest, UserInfoRestResponse requestUserInfo,
		ResourceCalculate requestTotalResource, String assignAuthCoe
	) {
		LocalDateTime assignDate = LocalDateTime.now();
		LicenseAssignAuthInfo licenseAssignAuthInfo = new LicenseAssignAuthInfo();
		licenseAssignAuthInfo.setAssignAuthCode(assignAuthCoe);
		licenseAssignAuthInfo.setUserId(allocateCheckRequest.getUserId());
		licenseAssignAuthInfo.setUuid(requestUserInfo.getUuid());
		licenseAssignAuthInfo.setUserName(requestUserInfo.getName());
		licenseAssignAuthInfo.setEmail(requestUserInfo.getEmail());
		licenseAssignAuthInfo.setAssignableCheckDate(assignDate);
		licenseAssignAuthInfo.setTotalProductCallTime(requestTotalResource.getTotalCallTime());
		licenseAssignAuthInfo.setTotalProductHit(requestTotalResource.getTotalDownloadHit());
		licenseAssignAuthInfo.setTotalProductStorage(requestTotalResource.getTotalStorageSize());
		licenseAssignAuthInfo.setExpiredDate(Duration.ofMinutes(LICENSE_ASSIGN_AUTH_CODE_TTL_MINUTE).getSeconds());
		licenseAssignAuthInfo.setRegularRequest(allocateCheckRequest.isRegularRequest());

		licenseAssignAuthInfoRepository.save(licenseAssignAuthInfo);
	}

	/**
	 * 상품 지급
	 *
	 * @param licenseAllocateRequest - 상품 지급 요청 데이터
	 * @return - 지급 상품 정보
	 */
	@Transactional
	public ApiResponse<LicenseProductAllocateResponse> licenseAllocateRequest(
		LicenseProductAllocateRequest licenseAllocateRequest
	) {
		// 1. 상품 지급 인증 정보 조회
		LicenseAssignAuthInfo licenseAssignAuthInfo = licenseAssignAuthInfoRepository.findById(
			licenseAllocateRequest.getAssignAuthCode())
			.orElseThrow(() -> new BillingServiceException(
				ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE));

		log.info("[FOUND ASSIGNMENT AUTH INFO]: {}", licenseAssignAuthInfo.toString());

		// 2. 지급 요청 사용자 정보 조회
		UserInfoRestResponse requestUserInfo = getUserInfoRestResponseByUserId(
			licenseAllocateRequest.getUserId(), ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT
		);

		// 4. 상품 지급 인증 정보 검증
		licenseAssignAuthInfoValidation(licenseAllocateRequest, licenseAssignAuthInfo, requestUserInfo);

		// 5. 지급 요청 사용자, 워크스페이스 정보 조회
		ApiResponse<WorkspaceInfoListResponse> workspaceApiResponse = this.workspaceRestService.getMyWorkspaceInfoList(
			requestUserInfo.getUuid(), 50);
		if (workspaceApiResponse.getCode() != 200 || workspaceApiResponse.getData().getWorkspaceList() == null) {
			log.error(USER_REST_SERVICE_ERROR_LOG_FORMAT, workspaceApiResponse.getMessage());
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
		}

		// 6. 마스터 워크스페이스 정보 추출
		WorkspaceInfoResponse workspaceInfo = workspaceApiResponse.getData().getWorkspaceList()
			.stream()
			.filter(w -> w.getRole().equals("MASTER")).findFirst()
			.orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED));

		// 7. 라이선스 플랜 정보 조회
		Optional<LicensePlan> userLicensePlan = licensePlanRepository.findByUserIdAndWorkspaceIdAndPlanStatus(
			requestUserInfo.getUuid(), workspaceInfo.getUuid(), PlanStatus.ACTIVE);

		// 8. 지급 요청 상품의 서비스 총 이용량 계산
		ResourceCalculate resourceCalculate = calculateAllocateProductResource(licenseAllocateRequest.getProductList());

		long calculateMaxCallTime = resourceCalculate.getTotalCallTime();
		long calculateMaxStorage = resourceCalculate.getTotalStorageSize();
		long calculateMaxHit = resourceCalculate.getTotalDownloadHit();

		// 8-1.지급 인증 정보 확인 - 통화 횟수, 용량, 다운로드 횟수
		licenseAllocatePropertyValidationCheck(licenseAssignAuthInfo, calculateMaxCallTime, calculateMaxStorage,
			calculateMaxHit
		);

		//9. 기존 활성화 된 라이선스 플랜 정보가 있는 경우
		if (userLicensePlan.isPresent()) {
			LicensePlan licensePlan = userLicensePlan.get();
			LocalDate licensePlanExpireDate = licensePlan.getEndDate().toLocalDate();

			//10. 추가 결제 요청 건인 경우, 상품 지급 건에 따른 서비스 이용 정보 갱신
			if (!licenseAllocateRequest.isRegularRequest()) {
				// 이용량 정보 갱신 (기존 이용량 + 추가 이용량)
				licensePlan.setMaxCallTime(licensePlan.getMaxCallTime() + calculateMaxCallTime);
				licensePlan.setMaxDownloadHit(licensePlan.getMaxDownloadHit() + calculateMaxHit);
				licensePlan.setMaxStorageSize(licensePlan.getMaxStorageSize() + calculateMaxStorage);

				// 라이선스 만료 전에 추가 구매 시, 추가 구매 일자 - 1일 기준으로 라이선스 만료 일자 변경
				// 기존 만료일이 A월 B일 일때, C일에 구매한 경우 A+1월 C-1일에 만료, (C > B)
				LocalDateTime additionalProductPurchaseDate = LocalDate.now()
					.plusMonths(1)
					.minusDays(1)
					.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);

				// 추가 라이선스 상품의 경우 라이선스 생성
				licenseRegisterByProduct(licenseAllocateRequest.getProductList(), licensePlan);

				// 라이선스 시작, 만료 일자 변경 (추가 구매일자 기준)
				licensePlan.setStartDate(LocalDateTime.now());
				licensePlan.setEndDate(additionalProductPurchaseDate);
			} else { // 정기 결제인 경우
				// 다음달 결제일 자정에 만료
				LocalDateTime nextLicenseExpiredDate = licensePlanExpireDate
					.plusMonths(1)
					.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);
				// 라이선스 시작, 만료일자 갱신
				licensePlan.setStartDate(LocalDateTime.now());
				licensePlan.setEndDate(nextLicenseExpiredDate);
			}

			// 결제 요청 식별자 및 국가 코드 갱신
			licensePlan.setPaymentId(licenseAllocateRequest.getPaymentId());
			licensePlan.setCountryCode(licenseAllocateRequest.getUserCountryCode());
			licensePlanRepository.save(licensePlan);

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
			.maxDownloadHit(FIRST_WORKSPACE_DOWNLOAD_HITS)
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
	 * @param licenseDeallocateRequest - 상품 지급 취소 요청 데이터
	 * @return - 상품 지급 취소 처리 결과 정보
	 */
	@Transactional
	public ApiResponse<LicenseProductDeallocateResponse> licenseDeallocateRequest(
		LicenseProductDeallocateRequest licenseDeallocateRequest
	) {
		ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(
			licenseDeallocateRequest.getUserId());
		if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData() == null) {
			log.error(USER_REST_SERVICE_ERROR_LOG_FORMAT, userInfoApiResponse.getMessage());
			log.error(USER_REST_SERVICE_ERROR_LOG_FORMAT, "사용자를 찾지 못함: " + licenseDeallocateRequest.toString());
			throw new BillingServiceException(ErrorCode.ERR_BILLING_LICENSE_DEALLOCATE_PLAN_NOT_FOUND);
		}
		// 1. 계정 정보 조회
		UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

		// 2. 라이선스 플랜 정보 조회
		LicensePlan licensePlan = licensePlanRepository.findByUserIdAndPaymentId(
			requestUserInfo.getUuid(),
			licenseDeallocateRequest.getPaymentId()
		).orElseThrow(() -> {
			log.error("[LICENSE_PLAN_NOT_FOUND][USER] - {}", requestUserInfo.toString());
			log.error("[LICENSE_PLAN_NOT_FOUND] - {}", licenseDeallocateRequest.toString());
			return new BillingServiceException(ErrorCode.ERR_BILLING_LICENSE_DEALLOCATE_PLAN_NOT_FOUND);
		});

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
	private void licenseAssignAuthInfoValidation(
		LicenseProductAllocateRequest licenseAllocateRequest, LicenseAssignAuthInfo licenseAssignAuthInfo,
		UserInfoRestResponse requestUserInfo
	) {
		if (!licenseAssignAuthInfo.getUserId().equals(licenseAllocateRequest.getUserId())
			|| !licenseAssignAuthInfo.getUuid().equals(requestUserInfo.getUuid())
			|| licenseAllocateRequest.isRegularRequest() != licenseAssignAuthInfo.isRegularRequest()) {
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
	private void licenseAllocatePropertyValidationCheck(
		LicenseAssignAuthInfo licenseAssignAuthInfo, Long calculateMaxCallTime, Long calculateMaxStorage,
		Long calculateMaxHit
	) {
		if (!licenseAssignAuthInfo.getTotalProductCallTime().equals(calculateMaxCallTime)
			|| !licenseAssignAuthInfo.getTotalProductHit().equals(calculateMaxHit)
			|| !licenseAssignAuthInfo.getTotalProductStorage().equals(calculateMaxStorage)) {
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE);
		}
	}

	/**
	 * 할당 요청 상품 정보 기반으로 등록
	 *
	 * @param allocateProductList - 지급 상품 정보 리스트
	 * @param licensePlan         - 상품 지급 대상 라이선스 플랜 정보
	 */
	@Transactional
	public void licenseRegisterByProduct(
		List<LicenseAllocateProductInfoResponse> allocateProductList, LicensePlan licensePlan
	) {
		if (allocateProductList == null || allocateProductList.isEmpty()) {
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
		}

		// 라이선스 상품 정보 추출
		List<LicenseAllocateProductInfoResponse> licenseProductList = allocateProductList.stream()
			.filter(p -> p.getProductType().getId().equals("product")).collect(Collectors.toList());

		if (!licenseProductList.isEmpty()) {
			registerLicenseProduct(licenseProductList, licensePlan);
		}
		// 추가 서비스 상품 정보 추출
		List<LicenseAllocateProductInfoResponse> serviceProductList = allocateProductList.stream()
			.filter(p -> p.getProductType().getId().equals("service")).collect(Collectors.toList());

		if (!serviceProductList.isEmpty()) {
			registerServiceProduct(serviceProductList, licensePlan);
		}
	}

	/**
	 * 라이선스 상품 정보 등록
	 *
	 * @param licenseProductList - 라이선스 상품 정보 리스트
	 * @param licensePlan        - 상품 지급 대상 라이선스 플랜 정보
	 */
	@Transactional
	public void registerLicenseProduct(
		List<LicenseAllocateProductInfoResponse> licenseProductList, LicensePlan licensePlan
	) {
		licenseProductList.forEach(productInfo -> {
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
	 * 서비스 상품 정보 등록
	 *
	 * @param serviceProductList - 서비스 상품 정보 리스트
	 * @param licensePlan        - 상품 지급 대상 라이선스 플랜 정보
	 */
	@Transactional
	public void registerServiceProduct(
		List<LicenseAllocateProductInfoResponse> serviceProductList, LicensePlan licensePlan
	) {
		ResourceCalculate serviceProductResourceInfo = calculateAllocateProductResource(serviceProductList);
		ServiceProduct serviceProduct = ServiceProduct.builder()
			.totalCallTime(serviceProductResourceInfo.getTotalCallTime())
			.totalDownloadHit(serviceProductResourceInfo.getTotalDownloadHit())
			.totalStorageSize(serviceProductResourceInfo.getTotalStorageSize())
			.licensePlan(licensePlan)
			.build();
		serviceProductRepository.save(serviceProduct);
	}

	/**
	 * 라이선스 생성
	 *
	 * @param licenseProduct - 제품 라이선스 정보
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

	/**
	 * 할당 요청 상품별 서비스 이용량 계산 처리
	 *
	 * @param allocateProductList - 할당 요청 상품 정보 리스트
	 * @return 상품별 서비스 이용량 총합 정보
	 */
	private ResourceCalculate calculateAllocateProductResource(
		List<LicenseAllocateProductInfoResponse> allocateProductList
	) {
		long calculateMaxCallTime = 0;
		long calculateMaxStorage = 0;
		long calculateMaxHit = 0;
		for (LicenseAllocateProductInfoResponse product : allocateProductList) {
			ProductTypeRequest productTypeInfo = product.getProductType();
			if (productTypeInfo.getName().equals("Remote") || productTypeInfo.getName().equals("CallTime")) {
				calculateMaxCallTime += product.getProductCallTime() * product.getProductAmount();
			} else if (productTypeInfo.getName().equals("Make") || productTypeInfo.getName().equals("Storage")) {
				calculateMaxStorage += product.getProductStorage() * product.getProductAmount();
			} else if (productTypeInfo.getName().equals("View") || productTypeInfo.getName().equals("Hit")) {
				calculateMaxHit += product.getProductHit() * product.getProductAmount();
			}
		}
		return new ResourceCalculate(calculateMaxCallTime, calculateMaxStorage, calculateMaxHit);
	}

	/**
	 * 할당 상품들의 서비스 이용 총 용량 정보 검증
	 *
	 * @param userId               - 할당 요청 사용자 식별자
	 * @param calculateMaxCallTime - 요청된 시간 관련 상품의 총 이용량  + 기존 라이선스 플랜의 서비스 이용량
	 * @param calculateMaxStorage  - 요청된 용량 관련 상품의 총 이용량  + 기존 라이선스 플랜의 서비스 이용량
	 * @param calculateMaxHit      - 요청된 다운로드 횟수 관련 상품의 총 이용량  + 기존 라이선스 플랜의 서비스 이용량
	 * @param licensePlan - 요청 사용자의 라이선스 플랜 정보
	 */
	private void allocateResourceValidation(
		long calculateMaxCallTime, long calculateMaxStorage, long calculateMaxHit, long userId,
		Optional<LicensePlan> licensePlan
	) {
		List<HashMap<String, Object>> detailMessage = new ArrayList<>();
		if (calculateMaxCallTime > MAX_CALL_TIME) {
			HashMap<String, Object> exceededCallTime = new HashMap<>();
			exceededCallTime.put("type", "callTime");
			exceededCallTime.put("requestAmount", calculateMaxCallTime);
			exceededCallTime.put("exceededAmount", calculateMaxCallTime - MAX_CALL_TIME);
			if (licensePlan.isPresent()) {
				exceededCallTime.put("availableAmount", MAX_CALL_TIME - licensePlan.get().getMaxCallTime());
			} else {
				exceededCallTime.put("availableAmount", MAX_CALL_TIME);
			}
			detailMessage.add(exceededCallTime);
		}
		if (calculateMaxStorage > MAX_STORAGE_AMOUNT) {
			HashMap<String, Object> exceededStorage = new HashMap<>();
			exceededStorage.put("type", "storage");
			exceededStorage.put("requestAmount", calculateMaxStorage);
			exceededStorage.put("exceededAmount", calculateMaxStorage - MAX_STORAGE_AMOUNT);
			if (licensePlan.isPresent()) {
				exceededStorage.put("availableAmount", MAX_STORAGE_AMOUNT - licensePlan.get().getMaxStorageSize());
			} else {
				exceededStorage.put("availableAmount", MAX_STORAGE_AMOUNT);
			}
			detailMessage.add(exceededStorage);
		}
		if (calculateMaxHit > MAX_DOWNLOAD_HITS) {
			HashMap<String, Object> exceededHit = new HashMap<>();
			exceededHit.put("type", "hit");
			exceededHit.put("requestAmount", calculateMaxHit);
			exceededHit.put("exceededAmount", calculateMaxHit - MAX_DOWNLOAD_HITS);
			if (licensePlan.isPresent()) {
				exceededHit.put("availableAmount", MAX_DOWNLOAD_HITS - licensePlan.get().getMaxDownloadHit());

			} else {
				exceededHit.put("availableAmount", MAX_DOWNLOAD_HITS);
			}
			detailMessage.add(exceededHit);
		}

		if (!detailMessage.isEmpty()) {
			throw new LicenseAllocateDeniedException(
				ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED, userId, detailMessage
			);
		}
	}

	/**
	 * 라이선스 관련 요청 사용자 정보 조회
	 * @param userId - 지급 요청 사용자 식별자
	 * @return - 사용자 정보
	 */
	private UserInfoRestResponse getUserInfoRestResponseByUserId(long userId, ErrorCode error) {
		ApiResponse<UserInfoRestResponse> userInfoApiResponse = userRestService.getUserInfoByUserPrimaryId(userId);
		if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData() == null) {
			log.error(USER_REST_SERVICE_ERROR_LOG_FORMAT, userInfoApiResponse.getMessage());
			log.error(USER_REST_SERVICE_ERROR_LOG_FORMAT, "사용자를 찾지 못함: " + userId);
			throw new BillingServiceException(error);
		}
		return userInfoApiResponse.getData();
	}

}
