package com.virnect.license.application;

import com.virnect.license.application.rest.content.ContentRestService;
import com.virnect.license.application.rest.workspace.WorkspaceRestService;
import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.licenseplan.PlanStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;
import com.virnect.license.domain.product.ProductType;
import com.virnect.license.domain.product.ServiceProduct;
import com.virnect.license.dto.ResourceCalculate;
import com.virnect.license.dto.UserLicenseDetailsInfo;
import com.virnect.license.dto.response.*;
import com.virnect.license.dto.rest.ContentResourceUsageInfoResponse;
import com.virnect.license.dto.rest.WorkspaceInfoResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageMetadataResponse;
import com.virnect.license.global.common.PageRequest;
import com.virnect.license.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final LicensePlanRepository licensePlanRepository;
    private final LicenseRepository licenseRepository;
    private final ContentRestService contentRestService;
    private final WorkspaceRestService workspaceRestService;
    private final ModelMapper modelMapper;


    /**
     * 워크스페이스 라이선스 플랜 정보 조회
     *
     * @param workspaceId - 워크스페이스 식별자
     * @return - 워크스페이스의 라이선스 플랜 정보
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
        Set<ServiceProduct> serviceProductList = licensePlanInfo.getServiceProductList();
        Map<Long, LicenseProductInfoResponse> licenseProductInfoMap = new HashMap<>();

        licenseProductList.forEach(licenseProduct -> {
            Product product = licenseProduct.getProduct();
            if (licenseProductInfoMap.containsKey(product.getId())) {
                LicenseProductInfoResponse licenseProductInfo = licenseProductInfoMap.get(product.getId());
                AtomicInteger unUsedLicenseAmount = new AtomicInteger();
                AtomicInteger usedLicenseAmount = new AtomicInteger();
                List<LicenseInfoResponse> licenseInfoList = getLicenseInfoResponses(licenseProduct, unUsedLicenseAmount, usedLicenseAmount);
                licenseProductInfo.setUseLicenseAmount(licenseProductInfo.getUseLicenseAmount() + usedLicenseAmount.get());
                licenseProductInfo.setUnUseLicenseAmount(licenseProductInfo.getUnUseLicenseAmount() + unUsedLicenseAmount.get());
                licenseProductInfo.getLicenseInfoList().addAll(licenseInfoList);
                licenseProductInfo.setQuantity(licenseProductInfo.getLicenseInfoList().size());
            } else {
                LicenseProductInfoResponse licenseProductInfo = new LicenseProductInfoResponse();
                AtomicInteger unUsedLicenseAmount = new AtomicInteger();
                AtomicInteger usedLicenseAmount = new AtomicInteger();
                // Product Info
                licenseProductInfo.setProductId(product.getId());
                licenseProductInfo.setProductName(product.getName());
                licenseProductInfo.setLicenseType(product.getProductType().getName());

                // Get License Information from license product
                List<LicenseInfoResponse> licenseInfoList = getLicenseInfoResponses(licenseProduct, unUsedLicenseAmount, usedLicenseAmount);

                licenseProductInfo.setLicenseInfoList(licenseInfoList);
                licenseProductInfo.setQuantity(licenseInfoList.size());
                licenseProductInfo.setUnUseLicenseAmount(unUsedLicenseAmount.get());
                licenseProductInfo.setUseLicenseAmount(usedLicenseAmount.get());
                licenseProductInfoMap.put(product.getId(), licenseProductInfo);
            }
        });


        ResourceCalculate serviceProductResource = serviceProductResourceCalculate(serviceProductList);

        ContentResourceUsageInfoResponse workspaceCurrentResourceUsageInfo = getContentResourceUsageInfoFromContentService(workspaceId, licensePlanInfo.getStartDate(), licensePlanInfo.getEndDate());
        log.info("[WORKSPACE_USAGE_RESOURCE_REPORT] -> {}", workspaceCurrentResourceUsageInfo.toString());
        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = modelMapper.map(licensePlan.get(), WorkspaceLicensePlanInfoResponse.class);
        workspaceLicensePlanInfoResponse.setMasterUserUUID(licensePlan.get().getUserId());
        workspaceLicensePlanInfoResponse.setLicenseProductInfoList(new ArrayList<>(licenseProductInfoMap.values()));
        // current used resource
        workspaceLicensePlanInfoResponse.setCurrentUsageDownloadHit(workspaceCurrentResourceUsageInfo.getTotalHit());
        workspaceLicensePlanInfoResponse.setCurrentUsageStorage(workspaceCurrentResourceUsageInfo.getStorageUsage());
        // add service product resource
        workspaceLicensePlanInfoResponse.setAddCallTime(serviceProductResource.getTotalCallTime());
        workspaceLicensePlanInfoResponse.setAddStorageSize(serviceProductResource.getTotalStorageSize());
        workspaceLicensePlanInfoResponse.setAddDownloadHit(serviceProductResource.getTotalDownloadHit());
        // default resource amount (maxResource - addResource)
        workspaceLicensePlanInfoResponse.setDefaultCallTime(licensePlanInfo.getMaxCallTime() - serviceProductResource.getTotalCallTime());
        workspaceLicensePlanInfoResponse.setDefaultStorageSize(licensePlanInfo.getMaxStorageSize() - serviceProductResource.getTotalStorageSize());
        workspaceLicensePlanInfoResponse.setDefaultDownloadHit(licensePlanInfo.getMaxDownloadHit() - serviceProductResource.getTotalDownloadHit());

        return new ApiResponse<>(workspaceLicensePlanInfoResponse);
    }

    /**
     * 추가 서비스 상품, 서비스 이용 정보 총 사용량 계산
     * @param serviceProductList - 추가 서비스 상품 서비스 이용량 정보
     * @return - 추가 서비스 상품, 서비스 이용 정보 총 사용량 정보
     */
    private ResourceCalculate serviceProductResourceCalculate(Set<ServiceProduct> serviceProductList) {
        long addCallTime = 0;
        long addStorage = 0;
        long addDownloadHit = 0;
        for (ServiceProduct serviceProduct : serviceProductList) {
            addCallTime += serviceProduct.getTotalCallTime();
            addStorage += serviceProduct.getTotalStorageSize();
            addDownloadHit += serviceProduct.getTotalDownloadHit();
        }
        return new ResourceCalculate(addCallTime, addStorage, addDownloadHit);
    }

    /**
     * 제품 라이선스 정보 조회
     *
     * @param licenseProduct      - 제품 라이선스 정보
     * @param unUsedLicenseAmount - 미사용 라이선스 수
     * @param usedLicenseAmount   - 사용 라이선스 수
     * @return - 라이선스 정보 목록
     */
    private List<LicenseInfoResponse> getLicenseInfoResponses(LicenseProduct licenseProduct, AtomicInteger unUsedLicenseAmount, AtomicInteger usedLicenseAmount) {
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
        return licenseInfoList;
    }

    /**
     * 워크스페이스 사용량 정보 조회
     *
     * @param workspaceId - 워크스페이스 식별자 정보
     * @return - 워크스페이스 서비스 사용량 정보
     */
    private ContentResourceUsageInfoResponse getContentResourceUsageInfoFromContentService(final String workspaceId, final LocalDateTime startDate, final LocalDateTime endDate) {
        ApiResponse<ContentResourceUsageInfoResponse> workspaceResourceUsageApiResponse = contentRestService.getContentResourceUsageInfoRequest(workspaceId, startDate, endDate);
        return workspaceResourceUsageApiResponse.getData();
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
     * 사용중인 라이선스 플랜 목록 정보 조회
     *
     * @param userId      - 사용자 식별자
     * @param pageRequest - 페이징 요청 정보
     * @return - 사용중인 라이선스 플랜 정보 목록
     */
    @Transactional(readOnly = true)
    public ApiResponse<MyLicensePlanInfoListResponse> getMyLicensePlanInfoList(String userId, PageRequest pageRequest) {
        Pageable pageable = pageRequest.of();
        log.info("{}", pageRequest.toString());
        Page<UserLicenseDetailsInfo> licenseDetailsInfoList = licenseRepository.findAllMyLicenseInfo(userId, pageable);
        // declare as set for remove duplicate data
        Set<MyLicensePlanInfoResponse> myLicensePlanInfoResponseSet = new HashSet<>();

        for (UserLicenseDetailsInfo detailsInfo : licenseDetailsInfoList) {
            ApiResponse<WorkspaceInfoResponse> workspaceInfoResponseMessage = workspaceRestService.getWorkspaceInfo(detailsInfo.getWorkspaceId());
            WorkspaceInfoResponse workspaceInfoResponse = workspaceInfoResponseMessage.getData();
            MyLicensePlanInfoResponse licensePlanInfoResponse = new MyLicensePlanInfoResponse();
            licensePlanInfoResponse.setWorkspaceId(workspaceInfoResponse.getUuid());
            licensePlanInfoResponse.setWorkspaceName(workspaceInfoResponse.getName());
            licensePlanInfoResponse.setPlanProduct(detailsInfo.getProductName());
            licensePlanInfoResponse.setRenewalDate(detailsInfo.getEndDate());
            licensePlanInfoResponse.setWorkspaceProfile(workspaceInfoResponse.getProfile());
            myLicensePlanInfoResponseSet.add(licensePlanInfoResponse);
        }

        PageMetadataResponse pageMetadataResponse = PageMetadataResponse.builder()
                .currentPage(pageable.getPageNumber())
                .currentSize(pageable.getPageSize())
                .totalPage(licenseDetailsInfoList.getTotalPages())
                .totalElements(licenseDetailsInfoList.getTotalElements())
                .build();

        // sorting and convert to list
        List<MyLicensePlanInfoResponse> myLicensePlanInfoList = myLicensePlanInfoResponseSet.stream()
                .sorted(getComparatorOfMyLicensePlainListResponse(pageRequest.getSort()))
                .collect(Collectors.toList());

        return new ApiResponse<>(new MyLicensePlanInfoListResponse(myLicensePlanInfoList, pageMetadataResponse));
    }

    /**
     * MyLicensePlainInfo 정렬 함수
     *
     * @param sortString - 정렬 필드 및 방법 (renewalDate, planProduct, workspaceName)
     * @return - 정렬된 MyLicensePlainInfoResponse 객체
     */
    private Comparator<? super MyLicensePlanInfoResponse> getComparatorOfMyLicensePlainListResponse(String sortString) {
        String[] sortQuery = sortString.split(",");
        String properties = sortQuery[0];
        String sort = sortQuery[1].toUpperCase();
        Comparator<? super MyLicensePlanInfoResponse> comparator;

        log.info("[CUSTOM_SORTING] - [{} -> {}]", properties, sort);
        if (properties.equals("planProduct")) {
            comparator = Comparator.comparing(MyLicensePlanInfoResponse::getPlanProduct);
        } else if (properties.equals("workspaceName")) {
            comparator = Comparator.comparing(MyLicensePlanInfoResponse::getWorkspaceName);
        } else {
            comparator = Comparator.comparing(MyLicensePlanInfoResponse::getRenewalDate).reversed();
        }

        if (sort.equals("DESC")) {
            return comparator.reversed();
        }
        return comparator;
    }
}
