package com.virnect.license.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virnect.license.dao.LicenseAssignAuthInfoRepository;
import com.virnect.license.dao.LicenseProductRepository;
import com.virnect.license.dao.LicenseRepository;
import com.virnect.license.dao.ProductTypeRepository;
import com.virnect.license.dao.coupon.CouponRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.dao.product.ProductRepository;
import com.virnect.license.domain.*;
import com.virnect.license.dto.request.*;
import com.virnect.license.dto.response.*;
import com.virnect.license.dto.response.admin.AdminCouponInfoListResponse;
import com.virnect.license.dto.response.admin.AdminCouponInfoResponse;
import com.virnect.license.dto.response.biling.ProductInfoListResponse;
import com.virnect.license.dto.response.biling.ProductInfoResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoListResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoResponse;
import com.virnect.license.dto.rest.UserInfoRestResponse;
import com.virnect.license.dto.rest.WorkspaceInfoListResponse;
import com.virnect.license.dto.rest.WorkspaceInfoResponse;
import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.AES256Utils;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageMetadataResponse;
import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.global.middleware.EncodingRequestResponse;
import com.virnect.license.infra.mail.EmailMessage;
import com.virnect.license.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final LicensePlanRepository licensePlanRepository;
    private final LicenseProductRepository licenseProductRepository;
    private final LicenseRepository licenseRepository;
    private final LicenseAssignAuthInfoRepository licenseAssignAuthInfoRepository;

    private final UserRestService userRestService;
    private final WorkspaceRestService workspaceRestService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper;

    private static long MAX_USER_AMOUNT = 9; // 9 명
    private static long MAX_CALL_TIME = 270; // 270 시간
    private static long MAX_STORAGE_AMOUNT = 90000; // 90 기가
    private static long MAX_DOWNLOAD_HITS = 1000000; // 10만 회
    private static long LICENSE_ASSIGN_AUTH_CODE_TTL_MINUTE = 30;
    private final String SECRET_KEY = "$37$15$TceNRIvGL$37$15$TceNRIvGL";


    /**
     * 이벤트 쿠폰 생성
     *
     * @param eventCouponRequest - 쿠폰 생성 요청 데이터
     * @return - 쿠폰 생성 정보
     */
    @Transactional
    public ApiResponse<EventCouponResponse> generateEventCoupon(EventCouponRequest eventCouponRequest) {
        // 1. 요청 사용자 정보 확인
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserId(eventCouponRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData().getEmail() == null) {
            log.info("User service error response: [{}]", userInfoApiResponse.getMessage());
            throw new LicenseServiceException(ErrorCode.ERR_CREATE_COUPON);
        }

        String LICENSE_TYPE_OF_2WEEK_FREE_COUPON = "2_WEEK_FREE_TRIAL_LICENSE";
        String COUPON_NAME = "2주 무료 사용 쿠폰";
        String serialKey = this.generateCouponSerialKey();

//         Check Duplicate Register Request
        boolean isAlreadyRegisterEventCoupon = this.licenseProductRepository.existsByLicenseType_NameAndAndCoupon_UserId(LICENSE_TYPE_OF_2WEEK_FREE_COUPON, eventCouponRequest.getUserId());

        if (isAlreadyRegisterEventCoupon) {
            throw new LicenseServiceException(ErrorCode.ERR_ALREADY_REGISTER_EVENT_COUPON);
        }

        Duration couponExpiredDuration = Duration.ofDays(14);
        LocalDateTime couponExpiredDate = LocalDateTime.now().plusDays(14);

        Coupon coupon = Coupon.builder()
                .company(eventCouponRequest.getCompanyName())
                .department(eventCouponRequest.getDepartment())
                .position(eventCouponRequest.getPosition())
                .companyEmail(eventCouponRequest.getCompanyEmail())
                .callNumber(eventCouponRequest.getCallNumber())
                .companySite(eventCouponRequest.getCompanySite())
                .companyService(eventCouponRequest.getCompanyService())
                .companyWorker(eventCouponRequest.getCompanyWorker())
                .content(eventCouponRequest.getContent())
                .marketInfoReceive(eventCouponRequest.getMarketInfoReceivePolicy())
                .duration(couponExpiredDuration.toDays())
                .expiredDate(couponExpiredDate)
                .periodType(CouponPeriodType.DAY)
                .couponStatus(CouponStatus.UNUSE)
                .name(COUPON_NAME)
                .serialKey(serialKey)
                .userId(eventCouponRequest.getUserId())
                .build();

        this.couponRepository.save(coupon);


        Set<Product> product = this.productRepository.findByProductType_NameAndNameIsIn("BASIC PLAN", Arrays.asList(eventCouponRequest.getProducts()))
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_CREATE_COUPON));

        for (Product couponProduct : product) {
            LicenseProduct licenseProduct = LicenseProduct.builder()
                    .product(couponProduct)
                    .coupon(coupon)
                    .quantity(1)
                    .build();

            this.licenseProductRepository.save(licenseProduct);
        }

        UserInfoRestResponse userInfoRestResponse = userInfoApiResponse.getData();
        EmailMessage message = new EmailMessage();
        message.setSubject("2주 무료 라이선스 쿠폰 발급 : 시리얼 코드를 확인하세요");
        message.setTo(userInfoRestResponse.getEmail());
        message.setMessage("Serial Key : " + serialKey);
        emailService.sendEmail(message);

        return new ApiResponse<>(new EventCouponResponse(true, coupon.getSerialKey(), coupon.getCreatedDate()));
    }

    /**
     * 이벤트 쿠폰 등록
     *
     * @param couponRegisterRequest
     * @return
     */

    @Transactional
    public ApiResponse<MyCouponInfoResponse> couponRegister(CouponRegisterRequest couponRegisterRequest) {
        // 1. 쿠폰 조회
        Coupon coupon = this.couponRepository.findByUserIdAndSerialKey(couponRegisterRequest.getUserId(), couponRegisterRequest.getCouponSerialKey())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_COUPON_NOT_FOUND));

        // 2. 쿠폰 등록 여부 검사 (등록일이 존재하는 경우)
        if (coupon.getRegisterDate() != null) {
            throw new LicenseServiceException(ErrorCode.ERR_COUPON_REGISTER_ALREADY_REGISTER);
        }

        // 3. 쿠폰 만료일 검사
        if (coupon.isExpired()) {
            throw new LicenseServiceException(ErrorCode.ERR_COUPON_EXPIRED);
        }

        // 4. 쿠폰 등록 일자 수정
        coupon.setRegisterDate(LocalDateTime.now());

        this.couponRepository.save(coupon);

        MyCouponInfoResponse registerCouponInfo = new MyCouponInfoResponse();
        registerCouponInfo.setId(coupon.getId());
        registerCouponInfo.setName(coupon.getName());
        registerCouponInfo.setStatus(coupon.getStatus());
        registerCouponInfo.setExpiredDate(coupon.getExpiredDate());
        registerCouponInfo.setRegisterDate(coupon.getCreatedDate());
        registerCouponInfo.setSerialKey(coupon.getSerialKey());

        return new ApiResponse<>(registerCouponInfo);
    }

    /**
     * 내 쿠폰 정보 리스트 조회
     *
     * @param userId
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public ApiResponse<MyCouponInfoListResponse> getMyCouponInfoList(String userId, Pageable pageable) {
        Page<Coupon> couponList = couponRepository.findByUserIdAndRegisterDateIsNotNull(userId, pageable);

        List<MyCouponInfoResponse> couponInfoList = couponList.stream().map(coupon -> {
            MyCouponInfoResponse myCouponInfo = new MyCouponInfoResponse();
            myCouponInfo.setId(coupon.getId());
            myCouponInfo.setName(coupon.getName());
            myCouponInfo.setStatus(coupon.getStatus());
            myCouponInfo.setRegisterDate(coupon.getCreatedDate());
            myCouponInfo.setExpiredDate(coupon.getExpiredDate());
            myCouponInfo.setSerialKey(coupon.getSerialKey());
            if (coupon.getStatus().equals(CouponStatus.USE)) {
                myCouponInfo.setStartDate(coupon.getStartDate());
                myCouponInfo.setEndDate(coupon.getEndDate());
            }
            return myCouponInfo;
        }).collect(Collectors.toList());

        PageMetadataResponse pageMetadataResponse = PageMetadataResponse.builder()
                .currentPage(pageable.getPageNumber())
                .currentSize(pageable.getPageSize())
                .totalPage(couponList.getTotalPages())
                .totalElements(couponList.getTotalElements())
                .build();

        return new ApiResponse<>(new MyCouponInfoListResponse(couponInfoList, pageMetadataResponse));
    }

    /**
     * 내 쿠폰 활성화
     *
     * @param couponActiveRequest
     * @return
     */
    @Transactional
    public ApiResponse<MyCouponInfoResponse> couponActiveHandler(CouponActiveRequest couponActiveRequest) {
        // 1. 활성화 할 쿠폰 찾기
        Coupon coupon = this.couponRepository.findByUserIdAndIdAndRegisterDateIsNotNull(couponActiveRequest.getUserId(), couponActiveRequest.getCouponId())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_COUPON_NOT_FOUND));

        // 2. 이미 사용된 쿠폰의 경우
        if (coupon.isUsed()) {
            throw new LicenseServiceException(ErrorCode.ERR_COUPON_ALREADY_ACTIVATED);
        }

        // 3. 쿠폰 기한이 만료된 경우
        if (coupon.isExpired()) {
            throw new LicenseServiceException(ErrorCode.ERR_COUPON_EXPIRED);
        }

        // 1. 라이선스 플랜 생성
        LicensePlan licensePlan = LicensePlan.builder()
                .userId(couponActiveRequest.getUserId())
                .workspaceId(couponActiveRequest.getWorkspaceId())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(coupon.getDuration()))
                .planStatus(PlanStatus.ACTIVE)
                .build();

        this.licensePlanRepository.save(licensePlan);

        coupon.setLicensePlan(licensePlan);
        coupon.setStatus(CouponStatus.USE);
        coupon.setStartDate(licensePlan.getStartDate());
        coupon.setEndDate(licensePlan.getEndDate());

        this.couponRepository.save(coupon);

        // 5. 쿠폰 기반으로 라이선스 정보 등록
        licenseRegisterByCouponProduct(coupon, licensePlan);

        // 6. 응답 데이터 설정
        MyCouponInfoResponse myCouponInfoResponse = new MyCouponInfoResponse();
        myCouponInfoResponse.setSerialKey(coupon.getSerialKey());
        myCouponInfoResponse.setId(coupon.getId());
        myCouponInfoResponse.setName(coupon.getName());
        myCouponInfoResponse.setStatus(coupon.getStatus());
        myCouponInfoResponse.setRegisterDate(coupon.getRegisterDate());
        myCouponInfoResponse.setExpiredDate(coupon.getExpiredDate());
        myCouponInfoResponse.setStartDate(licensePlan.getStartDate());
        myCouponInfoResponse.setEndDate(licensePlan.getEndDate());
        return new ApiResponse<>(myCouponInfoResponse);
    }

    /**
     * 쿠폰에 등록된 정보로 라이선스 생성
     *
     * @param coupon      쿠폰 정보
     * @param licensePlan - 신규 라이선스 플랜 정보
     */
    private void licenseRegisterByCouponProduct(Coupon coupon, LicensePlan licensePlan) {
        List<LicenseProduct> couponProductList = coupon.getCouponProductList();

        // 2. 쿠폰기반으로 쿠폰에 관련된 상품 정보 입력
        for (LicenseProduct couponProduct : couponProductList) {
            couponProduct.setLicensePlan(licensePlan);
            // 2-4. 라이선스 상품별 사용  가능한 라이선스 생성
            for (int i = 0; i < couponProduct.getQuantity(); i++) {
                License license = License.builder()
                        .status(LicenseStatus.UNUSE)
                        .serialKey(UUID.randomUUID().toString().toUpperCase())
                        .licenseProduct(couponProduct)
                        .build();
                this.licenseRepository.save(license);
            }
        }
    }

    /**
     * 쿠폰 시리얼 키 생성 (0,1은 O,I로 치환)
     *
     * @return - 시리얼 코드
     */
    private String generateCouponSerialKey() {
        return UUID.randomUUID().toString().replace("0", "O").replace("1", "I").toUpperCase();
    }

    /**
     * 전체 쿠폰 정보 조회
     *
     * @param pageable
     * @return - 쿠폰 정보 리스트
     */
    @Transactional(readOnly = true)
    public ApiResponse<AdminCouponInfoListResponse> getAllCouponInfo(Pageable pageable) {
        Page<Coupon> couponList = couponRepository.findAllCouponInfo(pageable);
        List<AdminCouponInfoResponse> adminCouponInfoList = couponList.stream()
                .map(c -> {
                    AdminCouponInfoResponse adminCouponInfoResponse = modelMapper.map(c, AdminCouponInfoResponse.class);
                    adminCouponInfoResponse.setProducts(c.getCouponProductList().stream().distinct().map(cp -> cp.getProduct().getName()).toArray(String[]::new));
                    return adminCouponInfoResponse;
                })
                .collect(Collectors.toList());

        PageMetadataResponse pageMetadataResponse = PageMetadataResponse.builder()
                .currentPage(pageable.getPageNumber())
                .currentSize(pageable.getPageSize())
                .totalPage(couponList.getTotalPages())
                .totalElements(couponList.getTotalElements())
                .build();

        return new ApiResponse<>(new AdminCouponInfoListResponse(adminCouponInfoList, pageMetadataResponse));
    }

    /**
     * 워크스페이스 라이선스 플랜 정보 조회
     *
     * @param workspaceId - 워크스페이스 식별자
     * @return
     */
    @Transactional(readOnly = true)
    public ApiResponse<WorkspaceLicensePlanInfoResponse> getWorkspaceLicensePlanInfo(String workspaceId) {
        Optional<LicensePlan> licensePlan = this.licensePlanRepository.findByWorkspaceIdAndPlanStatus(workspaceId, PlanStatus.ACTIVE);

        if (!licensePlan.isPresent()) {
            WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = new WorkspaceLicensePlanInfoResponse();
            return new ApiResponse<>(workspaceLicensePlanInfoResponse);
        }

        LicensePlan licensePlanInfo = licensePlan.get();
        Set<LicenseProduct> licenseProductList = licensePlanInfo.getLicenseProductList();
        List<LicenseProductInfoResponse> licenseProductInfoResponses = new ArrayList<>();


        licenseProductList.forEach(licenseProduct -> {
            LicenseProductInfoResponse licenseProductInfo = new LicenseProductInfoResponse();
            Product product = licenseProduct.getProduct();
            AtomicInteger unUsedLicenseAmount = new AtomicInteger();
            AtomicInteger usedLicenseAmount = new AtomicInteger();

            // Product Info
            licenseProductInfo.setProductId(product.getId());
            licenseProductInfo.setProductName(product.getName());
            licenseProductInfo.setLicenseType(product.getProductType().getName());

            // License Info
            List<LicenseInfoResponse> licenseInfoList = new ArrayList<>();
            licenseProduct.getLicenseList().forEach(license -> {
                LicenseInfoResponse licenseInfoResponse = new LicenseInfoResponse();
                licenseInfoResponse.setLicenseKey(license.getSerialKey());
                licenseInfoResponse.setStatus(license.getStatus());
                if (license.getStatus().equals(LicenseStatus.USE)) {
                    usedLicenseAmount.getAndIncrement();
                } else {
                    unUsedLicenseAmount.getAndIncrement();
                }
                licenseInfoResponse.setUserId(license.getUserId() == null ? "" : license.getUserId());
                licenseInfoResponse.setCreatedDate(license.getCreatedDate());
                licenseInfoResponse.setUpdatedDate(license.getUpdatedDate());
                licenseInfoList.add(licenseInfoResponse);
            });

            licenseProductInfo.setLicenseInfoList(licenseInfoList);
            licenseProductInfo.setQuantity(licenseInfoList.size());
            licenseProductInfo.setUnUseLicenseAmount(unUsedLicenseAmount.get());
            licenseProductInfo.setUseLicenseAmount(usedLicenseAmount.get());

            licenseProductInfoResponses.add(licenseProductInfo);
        });


        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = modelMapper.map(licensePlan.get(), WorkspaceLicensePlanInfoResponse.class);
        workspaceLicensePlanInfoResponse.setMasterUserUUID(licensePlan.get().getUserId());
        workspaceLicensePlanInfoResponse.setLicenseProductInfoList(licenseProductInfoResponses);

        return new ApiResponse<>(workspaceLicensePlanInfoResponse);
    }


    /**
     * 워크스페이스에서 내 라이선스 정보 가져오기
     *
     * @param userId      - 사용자 식별자
     * @param workspaceId - 워크스페이스 식별자
     * @return - 라이선스 정보 목록
     */
    @Transactional(readOnly = true)
    public ApiResponse<MyLicenseInfoListResponse> getMyLicenseInfoList(String userId, String workspaceId) {
        LicensePlan licensePlan = licensePlanRepository.findByWorkspaceIdAndPlanStatus(workspaceId, PlanStatus.ACTIVE)
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PLAN_NOT_FOUND));

        List<MyLicenseInfoResponse> myLicenseInfoResponseList = new ArrayList<>();
        for (LicenseProduct licenseProduct : licensePlan.getLicenseProductList()) {
            Product product = licenseProduct.getProduct();
            ProductType productType = product.getProductType();
            if (licenseProduct.getLicenseList() != null && !licenseProduct.getLicenseList().isEmpty()) {
                log.info(licenseProduct.getLicenseList().toString());
                licenseProduct.getLicenseList().stream().filter(license -> license.getUserId() != null && license.getUserId().equals(userId)).forEach(license -> {
                    MyLicenseInfoResponse licenseInfo = new MyLicenseInfoResponse();
                    licenseInfo.setId(license.getId());
                    licenseInfo.setSerialKey(license.getSerialKey());
                    licenseInfo.setCreatedDate(license.getCreatedDate());
                    licenseInfo.setProductName(product.getName());
                    licenseInfo.setUpdatedDate(license.getUpdatedDate());
                    licenseInfo.setLicenseType(productType.getName());
                    licenseInfo.setStatus(license.getStatus());
                    myLicenseInfoResponseList.add(licenseInfo);
                });
            }
        }
        return new ApiResponse<>(new MyLicenseInfoListResponse(myLicenseInfoResponseList));
    }

    /**
     * 워크스페이스내에서 사용자에게 플랜 할당, 해제
     *
     * @param workspaceId - 플랜할당(해제)이 이루어지는 워크스페이스 식별자
     * @param userId      - 플랜 할당(해제)을 받을 사용자 식별자
     * @param productName - 할당(해제) 을 받을 제품명(REMOTE, MAKE, VIEW 중 1)
     * @return - 할당받은 라이선스 정보 or 해제 성공 여부
     */
    @Transactional
    public ApiResponse grantWorkspaceLicenseToUser(String workspaceId, String userId, String productName, Boolean grant) {
        //워크스페이스 플랜찾기
        LicensePlan licensePlan = this.licensePlanRepository.findByWorkspaceIdAndPlanStatus(workspaceId, PlanStatus.ACTIVE)
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PLAN_NOT_FOUND));
        Set<LicenseProduct> licenseProductSet = licensePlan.getLicenseProductList();

        Product product = null;
        for (LicenseProduct licenseProduct : licenseProductSet) {
            if (licenseProduct.getProduct().getName().equalsIgnoreCase(productName)) {
                product = licenseProduct.getProduct();
            }
        }
        //워크스페이스가 가진 라이선스 중에 사용자가 요청한 제품 라이선스가 없는경우.
        if (product == null) {
            throw new LicenseServiceException(ErrorCode.ERR_LICENSE_PRODUCT_NOT_FOUND);
        }

        //라이선스 부여/해제
        License oldLicense = this.licenseRepository.findByUserIdAndLicenseProduct_LicensePlan_WorkspaceIdAndLicenseProduct_ProductAndStatus(userId, workspaceId, product, LicenseStatus.USE);
        if (grant) {
            if (oldLicense != null) {
                throw new LicenseServiceException(ErrorCode.ERR_LICENSE_ALREADY_GRANTED);
            }
            //부여 가능한 라이선스 찾기
            List<License> licenseList = this.licenseRepository.findAllByLicenseProduct_LicensePlan_WorkspaceIdAndLicenseProduct_LicensePlan_PlanStatusAndLicenseProduct_ProductAndStatus(
                    workspaceId, PlanStatus.ACTIVE, product, LicenseStatus.UNUSE);
            if (licenseList.isEmpty()) {
                throw new LicenseServiceException(ErrorCode.ERR_USEFUL_LICENSE_NOT_FOUND);
            }

            License updatedLicense = licenseList.get(0);
            updatedLicense.setUserId(userId);
            updatedLicense.setStatus(LicenseStatus.USE);
            this.licenseRepository.save(updatedLicense);

            MyLicenseInfoResponse myLicenseInfoResponse = this.modelMapper.map(updatedLicense, MyLicenseInfoResponse.class);
            return new ApiResponse<>(myLicenseInfoResponse);
        } else {
            oldLicense.setUserId(null);
            oldLicense.setStatus(LicenseStatus.UNUSE);
            this.licenseRepository.save(oldLicense);

            return new ApiResponse<>(true);
        }
    }

    /**
     * 전체 상품 정보 조회
     *
     * @return
     */
    @Transactional(readOnly = true)
    public ApiResponse<ProductInfoListResponse> getAllProductInfo() {
        List<Product> productList = productRepository.findAllByDisplayStatus(ProductDisplayStatus.DISPLAY);
        List<ProductInfoResponse> productInfoList = new ArrayList<>();
        productList.forEach(product -> {
            ProductInfoResponse productInfo = modelMapper.map(product, ProductInfoResponse.class);
            productInfo.setProductId(product.getId());
            productInfoList.add(productInfo);
        });
        return new ApiResponse<>(new ProductInfoListResponse(productInfoList));
    }

    /**
     * 전체 상품 타입 정보 조회
     *
     * @return
     */
    @Transactional(readOnly = true)
    public ApiResponse<ProductTypeInfoListResponse> getAllProductTypeInfo() {
        List<ProductType> productTypeList = productTypeRepository.findAll();
        List<ProductTypeInfoResponse> productTypeInfoList = new ArrayList<>();
        productTypeList.forEach(productType -> {
            ProductTypeInfoResponse productTypeInfoResponse = modelMapper.map(productType, ProductTypeInfoResponse.class);
            productTypeInfoList.add(productTypeInfoResponse);
        });
        return new ApiResponse<>(new ProductTypeInfoListResponse(productTypeInfoList));
    }

    /**
     * 상품 정보 수정
     *
     * @param updateRequest
     * @return
     */
    @Transactional
    public ApiResponse<ProductInfoResponse> updateProductInfo(ProductInfoUpdateRequest updateRequest) {
        Product product = productRepository.findById(updateRequest.getProductId())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_NOT_FOUND));

        // max call time update
        if (updateRequest.getProductMaxCallTime() > 0) {
            product.setMaxCallTime(updateRequest.getProductMaxCallTime());
        }
        // max download hits update
        if (updateRequest.getProductMaxDownloadHit() > 0) {
            product.setMaxDownloadHit(updateRequest.getProductMaxDownloadHit());
        }
        // max storage update
        if (updateRequest.getProductMaxStorageSize() > 0) {
            product.setMaxStorageSize(updateRequest.getProductMaxStorageSize());
        }
        // productType update
        if (updateRequest.getProductTypeId() > 0) {
            ProductType productType = productTypeRepository.findById(updateRequest.getProductTypeId())
                    .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_INFO_UPDATE));
            product.setProductType(productType);
        }

        // product display option update
        if (updateRequest.getProductDisplayStatus() != null) {
            product.setDisplayStatus(ProductDisplayStatus.valueOf(updateRequest.getProductDisplayStatus()));
        }

        productRepository.save(product);

        ProductInfoResponse productInfoResponse = modelMapper.map(product, ProductInfoResponse.class);
        productInfoResponse.setProductId(product.getId());
        return new ApiResponse<>(productInfoResponse);
    }

    /**
     * 상품 지급 여부 검사
     *
     * @param allocateCheckRequest
     * @return
     */
    @Transactional
    public EncodingRequestResponse licenseAllocateCheckRequest(LicenseAllocateCheckRequest allocateCheckRequest) {
        log.info("[BILLING][LICENSE ALLOCATE CHECK] -> [{}]", allocateCheckRequest.toString());
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(allocateCheckRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData().getEmail() == null) {
            log.info("User service error response: [{}]", userInfoApiResponse.getMessage());
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_LICENSE_SERVER_ERROR);
        }

        UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

        // 1. 현재 사용중인 라이선스 플랜 조회
        LicensePlan licensePlan = licensePlanRepository.findByUserIdAndPlanStatus(requestUserInfo.getUuid(), PlanStatus.ACTIVE);
        long calculateMaxCallTime = 0;
        long calculateMaxStorage = 0;
        long calculateMaxHit = 0;

        // 2. 현재 사용 중인 라이선스 플랜 정보가 있는 경우
        if (licensePlan != null) {
            //2.1  기존 라이선스 플랜의 서비스 이용 정보 추가
            calculateMaxCallTime += licensePlan.getMaxCallTime();
            calculateMaxStorage += licensePlan.getMaxStorageSize();
            calculateMaxHit += licensePlan.getMaxDownloadHit();
        }

        // 3. 상품 주문 정보 추가
        calculateMaxCallTime += allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductCallTime).sum();
        calculateMaxStorage += allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductStorage).sum();
        calculateMaxHit += allocateCheckRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductHit).sum();

        // 4. 최대 통화 수 , 최대 용량, 최대 다운로드 횟수 비교
        if (calculateMaxCallTime > MAX_CALL_TIME || calculateMaxStorage > MAX_STORAGE_AMOUNT || calculateMaxHit > MAX_DOWNLOAD_HITS) {
            throw new LicenseAllocateDeniedException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED, allocateCheckRequest.getUserId());
        } else {
            log.info("USER : [{}] -> CALCULATE CALL TIME : [{}] , CALCULATE STORAGE: [{}] , CALCULATE DOWNLOAD: [{}]", allocateCheckRequest.getUserId(), calculateMaxCallTime, calculateMaxStorage, calculateMaxHit);
        }

        // 5. 지급 인증 정보 생성
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

        // 6. 지급 인증 정보 저장
        licenseAssignAuthInfoRepository.save(licenseAssignAuthInfo);

        // 7. 지급 여부 결과 정보 생성
        LicenseProductAllocateCheckResponse checkResponse = new LicenseProductAllocateCheckResponse();
        checkResponse.setAssignable(true);
        checkResponse.setAssignAuthCode(assignAuthCoe);
        checkResponse.setUserId(allocateCheckRequest.getUserId());
        checkResponse.setAssignableCheckDate(LocalDateTime.now());
        ApiResponse<LicenseProductAllocateCheckResponse> apiResponse = new ApiResponse<>(checkResponse);
        EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
        try {
            encodingRequestResponse.setData(AES256Utils.encrypt(SECRET_KEY, objectMapper.writeValueAsString(apiResponse)));
            return encodingRequestResponse;
        } catch (JsonProcessingException e) {
            log.error("RESPONSE ENCRYPT FAIL.");
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED);
        }
    }

    /**
     * 상품 지급 취소
     *
     * @param licenseDeallocateRequest
     * @return
     */
    @Transactional
    public EncodingRequestResponse licenseDeallocateRequest(LicenseProductDeallocateRequest licenseDeallocateRequest) {
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(licenseDeallocateRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData().getEmail() == null) {
            log.info("User service error response: [{}]", userInfoApiResponse.getMessage());
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_LICENSE_SERVER_ERROR);
        }
        // 1. 계정 정보 조회
        UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

        // 2. 라이선스 플랜 정보 조회
        LicensePlan licensePlan = licensePlanRepository.findByUserIdAndPaymentId(requestUserInfo.getUuid(), licenseDeallocateRequest.getPaymentId())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_LICENSE_SERVER_ERROR));

        // 3. 라이선스 플랜 정보 수정 기록 및 비활성화
        licensePlan.setModifiedUser(licenseDeallocateRequest.getOperatedBy());
        licensePlan.setPlanStatus(PlanStatus.INACTIVE);
        licensePlanRepository.save(licensePlan);

        LicenseProductDeallocateResponse deallocateResponse = new LicenseProductDeallocateResponse();
        deallocateResponse.setPaymentId(licenseDeallocateRequest.getPaymentId());
        deallocateResponse.setUserId(licenseDeallocateRequest.getUserId());
        deallocateResponse.setDeallocatedDate(LocalDateTime.now());
        ApiResponse<LicenseProductDeallocateResponse> apiResponse = new ApiResponse<>(deallocateResponse);
        EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
        try {
            encodingRequestResponse.setData(AES256Utils.encrypt(SECRET_KEY, objectMapper.writeValueAsString(apiResponse)));
            return encodingRequestResponse;
        } catch (JsonProcessingException e) {
            log.error("[LICENSE_PRODUCT_DEALLOCATE_RESPONSE][ENCRYPT FAIL.]");
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED);
        }
    }

    /**
     * 상품 지급
     *
     * @param licenseAllocateRequest
     * @return
     */
    @Transactional
    public EncodingRequestResponse licenseAllocateRequest(LicenseProductAllocateRequest licenseAllocateRequest) {
        // 1. 상품 지급 인증 정보 조회
        LicenseAssignAuthInfo licenseAssignAuthInfo = licenseAssignAuthInfoRepository.findById(licenseAllocateRequest.getAssignAuthCode())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE));

        log.info("[FOUND ASSIGNMENT AUTH INFO]: {}", licenseAssignAuthInfo.toString());

        // 2. 지급 요청 사용자 정보 조회
        ApiResponse<UserInfoRestResponse> userInfoApiResponse = this.userRestService.getUserInfoByUserPrimaryId(licenseAllocateRequest.getUserId());
        if (userInfoApiResponse.getCode() != 200 || userInfoApiResponse.getData().getEmail() == null) {
            log.info("[USER REST SERVICE ERROR RESPONSE]: [{}]", userInfoApiResponse.getMessage());
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
        }

        UserInfoRestResponse requestUserInfo = userInfoApiResponse.getData();

        // 4. 상품 지급 인증 정보 검증
        licenseAssignAuthInfoValidation(licenseAllocateRequest, licenseAssignAuthInfo, requestUserInfo);

        // 5. 지급 요청 사용자, 워크스페이스 정보 조회
        ApiResponse<WorkspaceInfoListResponse> workspaceApiResponse = this.workspaceRestService.getMyWorkspaceInfoList(requestUserInfo.getUuid());
        if (workspaceApiResponse.getCode() != 200 || workspaceApiResponse.getData().getWorkspaceList() == null) {
            log.info("User service error response: [{}]", workspaceApiResponse.getMessage());
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
        }

        // 6. 마스터 워크스페이스 정보 추출
        WorkspaceInfoResponse workspaceInfo = workspaceApiResponse.getData().getWorkspaceList()
                .stream()
                .filter(w -> w.getRole().equals("MASTER")).findFirst()
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED));

        // 7. 라이선스 플랜 정보 조회
        Optional<LicensePlan> userLicensePlan = licensePlanRepository.findByUserIdAndWorkspaceIdAndPlanStatus(requestUserInfo.getUuid(), workspaceInfo.getUuid(), PlanStatus.ACTIVE);

        // 8. 지급 상품 서비스 이용 정보 계산 ( 통화 시간, 용량, 다운로드 횟수 )
        Long calculateMaxCallTime = licenseAllocateRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductCallTime).sum();
        Long calculateMaxStorage = licenseAllocateRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductStorage).sum();
        Long calculateMaxHit = licenseAllocateRequest.getProductList().stream().mapToLong(LicenseAllocateProductInfoResponse::getProductHit).sum();

        // 8.지급 인증 정보 확인 - 통화 횟수, 용량, 다운로드 횟수
        licenseAllocatePropertyValidationCheck(licenseAssignAuthInfo, calculateMaxCallTime, calculateMaxStorage, calculateMaxHit);

        //9. 기존 라이선스 플랜 정보가 있는 경우
        if (userLicensePlan.isPresent()) {
            LicensePlan licensePlan = userLicensePlan.get();
            licenseRegisterByProduct(licenseAllocateRequest.getProductList(), licensePlan);
            licensePlan.setMaxCallTime(licensePlan.getMaxCallTime() + calculateMaxCallTime);
            licensePlan.setMaxDownloadHit(licensePlan.getMaxDownloadHit() + calculateMaxHit);
            licensePlan.setMaxStorageSize(licensePlan.getMaxStorageSize() + calculateMaxStorage);
            // 베이직 플랜 구매 시(Make, Remote) 활성화 계정  갯수 9개 제공
//            licensePlan.setMaxUserAmount(licensePlan.getMaxUserAmount() + calculateMaxUserAmount);
            licensePlan.setPaymentId(licenseAllocateRequest.getPaymentId());
            licensePlan.setEndDate(licensePlan.getEndDate().plusDays(30));
            licensePlan.setCountryCode(licenseAllocateRequest.getUserCountryCode());
            licensePlanRepository.save(licensePlan);

            // 10. 지급 상품 라이선스 생성
            licenseRegisterByProduct(licenseAllocateRequest.getProductList(), licensePlan);

            LicenseProductAllocateResponse allocateResponse = new LicenseProductAllocateResponse();
            allocateResponse.setUserId(licenseAllocateRequest.getUserId());
            allocateResponse.setPaymentId(licenseAllocateRequest.getPaymentId());
            allocateResponse.setAllocatedDate(licensePlan.getUpdatedDate());
            allocateResponse.setAllocatedProductList(licenseAllocateRequest.getProductList());
            EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
            licenseAssignAuthInfoRepository.deleteById(licenseAllocateRequest.getAssignAuthCode());
            ApiResponse<LicenseProductAllocateResponse> apiResponse = new ApiResponse<>(allocateResponse);
            try {
                encodingRequestResponse.setData(AES256Utils.encrypt(SECRET_KEY, objectMapper.writeValueAsString(apiResponse)));
                return encodingRequestResponse;
            } catch (JsonProcessingException e) {
                log.error("[LICENSE_PRODUCT_DEALLOCATE_RESPONSE][ENCRYPT FAIL.]");
                throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED);
            }
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

        EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
        ApiResponse<LicenseProductAllocateResponse> apiResponse = new ApiResponse<>(allocateResponse);
        try {
            encodingRequestResponse.setData(AES256Utils.encrypt(SECRET_KEY, objectMapper.writeValueAsString(apiResponse)));
            return encodingRequestResponse;
        } catch (JsonProcessingException e) {
            log.error("[LICENSE_PRODUCT_DEALLOCATE_RESPONSE][ENCRYPT FAIL.]");
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED);
        }
    }

    /**
     * 라이선스 상품 지급 인증 정보 및 지급 요청 데이터 검증
     *
     * @param licenseAllocateRequest - 라이선스 지급 요청 정보
     * @param licenseAssignAuthInfo  - 라이선스 상품 지급 인증 정보
     * @param requestUserInfo        - 지급 요청 사용자 정보
     */
    private void licenseAssignAuthInfoValidation(LicenseProductAllocateRequest licenseAllocateRequest, LicenseAssignAuthInfo licenseAssignAuthInfo, UserInfoRestResponse requestUserInfo) {
        if (!licenseAssignAuthInfo.getUserId().equals(licenseAllocateRequest.getUserId()) || !licenseAssignAuthInfo.getUuid().equals(requestUserInfo.getUuid())) {
            log.info("[LICENSE PRODUCT ALLOCATE AUTHENTICATION INFO CHECK] - FAIL.");
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE);
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
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE);
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
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
        }
        productList.stream()
                .filter(p -> !p.getProductType().getId().equals("service"))
                .forEach(productInfo -> {
                    Product product = this.productRepository.findById(productInfo.getProductId())
                            .orElseThrow(() -> {
                                log.info("ASSIGN REQUEST PRODUCT NOT FOUND -> [{}] ", productInfo.toString());
                                return new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
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

    /**
     * 상품 삭제
     *
     * @param productId
     * @return
     */
    @Transactional
    public ApiResponse<ProductInfoListResponse> deleteProduct(long productId) {
        long result = productRepository.updateProductDisplayStatusToHide(productId);
        if (result <= 0) {
            throw new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_DISABLE);
        }
        return getAllProductInfo();
    }

    /**
     * 상품 타입정보 수정
     *
     * @param productTypeUpdateRequest
     * @return
     */
    @Transactional
    public ApiResponse<ProductTypeInfoListResponse> updateProductTypeInfo(ProductTypeUpdateRequest productTypeUpdateRequest) {
        ProductType productType = productTypeRepository.findById(productTypeUpdateRequest.getProductTypeId())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_TYPE_INFO_UPDATE));
        productType.setName(productTypeUpdateRequest.getProductTypeName());
        productTypeRepository.save(productType);

        return getAllProductTypeInfo();
    }

    /**
     * 상품 생성
     *
     * @param createNewProductRequest
     * @return
     */
    @Transactional
    public ApiResponse<ProductInfoListResponse> createNewProductHandler(CreateNewProductRequest createNewProductRequest) {
        ProductType productType = this.productTypeRepository.findById(createNewProductRequest.getProductTypeId())
                .orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_BILLING_PRODUCT_CREATE));
        Product newProduct = Product.builder()
                .name(createNewProductRequest.getProductName())
                .maxStorageSize(createNewProductRequest.getMaxStorageSize())
                .maxDownloadHit(createNewProductRequest.getMaxDownloadHit())
                .maxCallTime(createNewProductRequest.getMaxCallTime())
                .productType(productType)
                .build();
        productRepository.save(newProduct);

        return getAllProductInfo();
    }

    /**
     * 상품 타입 생성
     *
     * @param createNewProductTypeRequest
     * @return
     */
    @Transactional
    public ApiResponse<ProductTypeInfoListResponse> createNewProductTypeHandler(CreateNewProductTypeRequest createNewProductTypeRequest) {
        ProductType productType = new ProductType(createNewProductTypeRequest.getProductTypeName());
        productTypeRepository.save(productType);
        return getAllProductTypeInfo();
    }
}
