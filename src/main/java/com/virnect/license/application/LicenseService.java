package com.virnect.license.application;

import com.virnect.license.dao.*;
import com.virnect.license.domain.*;
import com.virnect.license.dto.request.CouponActiveRequest;
import com.virnect.license.dto.request.CouponRegisterRequest;
import com.virnect.license.dto.request.EventCouponRequest;
import com.virnect.license.dto.response.*;
import com.virnect.license.dto.response.admin.AdminCouponInfoListResponse;
import com.virnect.license.dto.response.admin.AdminCouponInfoResponse;
import com.virnect.license.dto.response.biling.ProductInfoListResponse;
import com.virnect.license.dto.response.biling.ProductInfoResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoListResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoResponse;
import com.virnect.license.dto.rest.UserInfoRestResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageMetadataResponse;
import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.infra.mail.EmailMessage;
import com.virnect.license.infra.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    private final UserRestService userRestService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;

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


        Set<Product> product = this.productRepository.findByProductType_NameAndNameIsIn("BASIC", Arrays.asList(eventCouponRequest.getProducts()))
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

        // 2. 쿠폰 기반으로 쿠폰에 관련된 상품 정보 입력
        for (LicenseProduct couponProduct : couponProductList) {
            couponProduct.setLicensePlan(licensePlan);
            // 2-4. 라이선스 상품별 사용가능한 라이선스 생성
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
                licenseInfoResponse.setUserId(license.getUserId() == null ? "" : license.getUserId());
                licenseInfoResponse.setCreatedDate(license.getCreatedDate());
                licenseInfoResponse.setUpdatedDate(license.getUpdatedDate());
                licenseInfoList.add(licenseInfoResponse);
            });

            licenseProductInfo.setLicenseInfoList(licenseInfoList);
            licenseProductInfo.setQuantity(licenseInfoList.size());

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
            licenseProduct.getLicenseList().stream().filter(license -> license.getUserId().equals(userId)).forEach(license -> {
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
            myLicenseInfoResponse.setLicenseType(updatedLicense.getLicenseProduct().getLicenseType().getName());
            return new ApiResponse<>(myLicenseInfoResponse);
        } else {
            oldLicense.setUserId(null);
            oldLicense.setStatus(LicenseStatus.UNUSE);
            this.licenseRepository.save(oldLicense);

            return new ApiResponse<>(true);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<ProductInfoListResponse> getAllProductInfo() {
        List<Product> productList = productRepository.findAll();
        List<ProductInfoResponse> productInfoList = new ArrayList<>();
        productList.forEach(product -> {
            ProductInfoResponse productInfo = modelMapper.map(product, ProductInfoResponse.class);
            productInfo.setProductId(product.getId());
            productInfoList.add(productInfo);
        });
        return new ApiResponse<>(new ProductInfoListResponse(productInfoList));
    }

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


}
