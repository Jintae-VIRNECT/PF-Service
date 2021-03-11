package com.virnect.license.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.rest.billing.PayAPIService;
import com.virnect.license.application.rest.content.ContentRestService;
import com.virnect.license.application.rest.workspace.WorkspaceRestService;
import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.dao.licenseproduct.LicenseProductRepository;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.licenseplan.PlanStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.LicenseProductStatus;
import com.virnect.license.domain.product.Product;
import com.virnect.license.dto.ResourceCalculate;
import com.virnect.license.dto.UserLicenseDetailsInfo;
import com.virnect.license.dto.license.response.LicenseInfoResponse;
import com.virnect.license.dto.license.response.LicenseProductInfoResponse;
import com.virnect.license.dto.license.response.LicenseSecessionResponse;
import com.virnect.license.dto.license.response.MyLicenseInfoListResponse;
import com.virnect.license.dto.license.response.MyLicenseInfoResponse;
import com.virnect.license.dto.license.response.MyLicensePlanInfoListResponse;
import com.virnect.license.dto.license.response.MyLicensePlanInfoResponse;
import com.virnect.license.dto.license.response.WorkspaceLicensePlanInfoResponse;
import com.virnect.license.dto.rest.content.ContentResourceUsageInfoResponse;
import com.virnect.license.dto.rest.user.WorkspaceInfoResponse;
import com.virnect.license.event.license.LicenseExpiredEvent;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageMetadataResponse;
import com.virnect.license.global.common.PageRequest;
import com.virnect.license.global.error.ErrorCode;

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
	private final LicenseProductRepository licenseProductRepository;
	private final ModelMapper modelMapper;
	private final PayAPIService payAPIService;
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * 워크스페이스 라이선스 플랜 정보 조회
	 *
	 * @param workspaceId - 워크스페이스 식별자
	 * @return - 워크스페이스의 라이선스 플랜 정보
	 */
	@Transactional(readOnly = true)
	public WorkspaceLicensePlanInfoResponse getWorkspaceLicensePlanInfo(String workspaceId) {
		Optional<LicensePlan> licensePlanInfo = licensePlanRepository.findByWorkspaceIdAndPlanStatusNot(
			workspaceId, PlanStatus.TERMINATE
		);

		if (!licensePlanInfo.isPresent()) {
			return new WorkspaceLicensePlanInfoResponse();
		}

		LicensePlan licensePlan = licensePlanInfo.get();
		List<LicenseProduct> licenseProductList = licenseProductRepository.findAllProductLicenseInfoByLicensePlan(
			licensePlan);
		List<LicenseProduct> serviceProductList = licenseProductRepository.findAllServiceLicenseInfoByLicensePlan(
			licensePlan);

		ResourceCalculate serviceProductResource = licenseProductResourceCalculate(serviceProductList);
		ResourceCalculate defaultProductResource = licenseProductResourceCalculate(licenseProductList);

		Map<Long, LicenseProductInfoResponse> licenseProductInfoMap = new HashMap<>();

		// TODO: 2020-09-04 리팩토링 필요
		licenseProductList.forEach(licenseProduct -> {
			Product product = licenseProduct.getProduct();
			if (licenseProductInfoMap.containsKey(product.getId())) {
				LicenseProductInfoResponse licenseProductInfo = licenseProductInfoMap.get(product.getId());
				AtomicInteger unUsedLicenseAmount = new AtomicInteger();
				AtomicInteger usedLicenseAmount = new AtomicInteger();
				List<LicenseInfoResponse> licenseInfoList = getLicenseInfoResponses(
					licenseProduct, unUsedLicenseAmount, usedLicenseAmount
				);
				licenseProductInfo.setUseLicenseAmount(
					licenseProductInfo.getUseLicenseAmount() + usedLicenseAmount.get()
				);
				licenseProductInfo.setUnUseLicenseAmount(
					licenseProductInfo.getUnUseLicenseAmount() + unUsedLicenseAmount.get()
				);
				licenseProductInfo.getLicenseInfoList().addAll(licenseInfoList);
				licenseProductInfo.setQuantity(licenseProductInfo.getQuantity());
				licenseProductInfo.setProductStatus(licenseProduct.getStatus());
			} else {
				LicenseProductInfoResponse licenseProductInfo = new LicenseProductInfoResponse();
				AtomicInteger unUsedLicenseAmount = new AtomicInteger();
				AtomicInteger usedLicenseAmount = new AtomicInteger();
				// Product Info
				licenseProductInfo.setProductId(product.getBillProductId());
				licenseProductInfo.setProductName(product.getName());
				// licenseProductInfo.setLicenseType(product.getProductType().getName());

				// Get License Information from license product
				List<LicenseInfoResponse> licenseInfoList = getLicenseInfoResponses(
					licenseProduct, unUsedLicenseAmount, usedLicenseAmount
				);

				licenseProductInfo.setLicenseInfoList(licenseInfoList);
				licenseProductInfo.setQuantity(licenseProduct.getQuantity());
				licenseProductInfo.setUnUseLicenseAmount(unUsedLicenseAmount.get());
				licenseProductInfo.setUseLicenseAmount(usedLicenseAmount.get());
				licenseProductInfo.setProductStatus(licenseProduct.getStatus());
				licenseProductInfoMap.put(product.getId(), licenseProductInfo);
			}
		});

		ContentResourceUsageInfoResponse workspaceCurrentResourceUsageInfo = getContentResourceUsageInfoFromContentService(
			workspaceId, licensePlan.getStartDate(), licensePlan.getEndDate());
		log.info("[WORKSPACE_USAGE_RESOURCE_REPORT] -> {}", workspaceCurrentResourceUsageInfo.toString());
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = modelMapper.map(
			licensePlanInfo.get(),
			WorkspaceLicensePlanInfoResponse.class
		);
		workspaceLicensePlanInfoResponse.setMasterUserUUID(licensePlanInfo.get().getUserId());
		workspaceLicensePlanInfoResponse.setLicenseProductInfoList(new ArrayList<>(licenseProductInfoMap.values()));
		// current used resource
		workspaceLicensePlanInfoResponse.setCurrentUsageDownloadHit(workspaceCurrentResourceUsageInfo.getTotalHit());
		workspaceLicensePlanInfoResponse.setCurrentUsageStorage(workspaceCurrentResourceUsageInfo.getStorageUsage());
		// add service product resource
		workspaceLicensePlanInfoResponse.setAddCallTime(serviceProductResource.getTotalCallTime());
		workspaceLicensePlanInfoResponse.setAddStorageSize(serviceProductResource.getTotalStorageSize());
		workspaceLicensePlanInfoResponse.setAddDownloadHit(serviceProductResource.getTotalDownloadHit());
		// default resource amount
		workspaceLicensePlanInfoResponse.setDefaultCallTime(defaultProductResource.getTotalCallTime());
		workspaceLicensePlanInfoResponse.setDefaultStorageSize(defaultProductResource.getTotalStorageSize());
		workspaceLicensePlanInfoResponse.setDefaultDownloadHit(defaultProductResource.getTotalDownloadHit());

		return workspaceLicensePlanInfoResponse;
	}

	/**
	 * 상품별 서비스 이용 정보 총 사용량 계산
	 *
	 * @param licenseProductList - 상품 별 서비스 이용량 정보
	 * @return - 상품별 서비스 이용 정보 총 사용량 정보
	 */
	private ResourceCalculate licenseProductResourceCalculate(List<LicenseProduct> licenseProductList) {
		long addCallTime = 0;
		long addStorage = 0;
		long addDownloadHit = 0;
		for (LicenseProduct licenseProd : licenseProductList) {
			addCallTime += licenseProd.getCallTime();
			addStorage += licenseProd.getStorageSize();
			addDownloadHit += licenseProd.getDownloadHit();
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
	private List<LicenseInfoResponse> getLicenseInfoResponses(
		LicenseProduct licenseProduct, AtomicInteger unUsedLicenseAmount, AtomicInteger usedLicenseAmount
	) {
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
	private ContentResourceUsageInfoResponse getContentResourceUsageInfoFromContentService(
		final String workspaceId, final LocalDateTime startDate, final LocalDateTime endDate
	) {
		ApiResponse<ContentResourceUsageInfoResponse> workspaceResourceUsageApiResponse =
			contentRestService.getContentResourceUsageInfoRequest(workspaceId, startDate, endDate);
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
		// Todo: 페이징 기능 넣기
		LicensePlan licensePlan = licensePlanRepository.findByWorkspaceIdAndPlanStatusNot(
			workspaceId, PlanStatus.TERMINATE)
			.orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PLAN_NOT_FOUND));

		List<MyLicenseInfoResponse> myLicenseInfoResponseList = new ArrayList<>();
		for (LicenseProduct licenseProduct : licensePlan.getLicenseProductList()) {
			Product product = licenseProduct.getProduct();
			// ProductType productType = product.getProductType();
			if (licenseProduct.getLicenseList() != null && !licenseProduct.getLicenseList().isEmpty()) {
				licenseProduct.getLicenseList()
					.stream()
					.filter(license -> license.getUserId() != null && license.getUserId().equals(userId))
					.forEach(license -> {
						MyLicenseInfoResponse licenseInfo = new MyLicenseInfoResponse();
						licenseInfo.setId(license.getId());
						licenseInfo.setSerialKey(license.getSerialKey());
						licenseInfo.setCreatedDate(license.getCreatedDate());
						licenseInfo.setProductName(product.getName());
						licenseInfo.setUpdatedDate(license.getUpdatedDate());
						licenseInfo.setStatus(license.getStatus());
						licenseInfo.setProductPlanStatus(licenseProduct.getStatus().toString());
						myLicenseInfoResponseList.add(licenseInfo);
					});
			}
		}
		return new ApiResponse<>(new MyLicenseInfoListResponse(myLicenseInfoResponseList));
	}

	/**
	 * 워크스페이스내에서 사용자에게 플랜 할당
	 *
	 * @param workspaceId - 플랜 할당 이 이루어지는 워크스페이스 식별자
	 * @param userId      - 플랜 할당을 받을 사용자 식별자
	 * @param productName - 할당을 받을 제품명(REMOTE, MAKE, VIEW 중 1)
	 * @return - 할당받은 라이선스 정보
	 */
	@Transactional
	public MyLicenseInfoResponse userLicenseAllocateRequestHandler(
		String workspaceId, String userId, String productName
	) {
		//워크스페이스 플랜찾기 ( 비활성화 또는 활성화)
		LicensePlan licensePlan = licensePlanRepository.findByWorkspaceIdAndPlanStatus(
			workspaceId, PlanStatus.ACTIVE
		).orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PLAN_NOT_FOUND));

		LicenseProduct licenseProduct = licenseProductRepository.findByLicensePlanAndProduct_Name(
			licensePlan, productName
		).orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PRODUCT_NOT_FOUND));

		License currentUserLicense = licenseRepository.licenseAllocationRevokeByUserIdAndLicenseProductId(
			userId, licenseProduct.getId());

		// 라이선스 부여, 이미 부여되어있는 경우
		if (currentUserLicense != null) {
			throw new LicenseServiceException(ErrorCode.ERR_LICENSE_ALREADY_GRANTED);
		}

		long usedLicenseAmount = licenseProduct.getLicenseList()
			.stream()
			.filter(l -> l.getStatus() == LicenseStatus.USE)
			.count();

		log.info(
			"[LICENSE_STATUS] - LICENSE_PRODUCT_AMOUNT: {} , USED_LICENSE_AMOUNT: {} , RESULT: {}",
			licenseProduct.getQuantity(), usedLicenseAmount, licenseProduct.getQuantity() >= usedLicenseAmount
		);

		// 현재 할당된 라이선스 수가 부여된 라이선스 갯수 보다 크거나(축소된 경우) 같은 경우
		if (licenseProduct.getQuantity() <= usedLicenseAmount) {
			log.error("[LICENSE_ALLOCATE_FAIL] - LICENSE_PRODUCT_AMOUNT: {} , USED_LICENSE_AMOUNT: {} , RESULT: {}",
				licenseProduct.getQuantity(), usedLicenseAmount, licenseProduct.getQuantity() >= usedLicenseAmount
			);
			throw new LicenseServiceException(ErrorCode.ERR_USEFUL_LICENSE_NOT_FOUND);
		}

		Optional<License> allocatableLicenseInfo = licenseRepository.findAllocatableLicensesByLicenseProduct(
			licenseProduct);

		if (!allocatableLicenseInfo.isPresent()) {
			throw new LicenseServiceException(ErrorCode.ERR_USEFUL_LICENSE_NOT_FOUND);
		}

		License updatedLicense = allocatableLicenseInfo.get();
		updatedLicense.setUserId(userId);
		updatedLicense.setStatus(LicenseStatus.USE);
		licenseRepository.save(updatedLicense);

		return modelMapper.map(
			updatedLicense,
			MyLicenseInfoResponse.class
		);
	}

	/**
	 * 사용자 라이선스 할당 해제
	 * @param workspaceId - 워크스페이스 아이디
	 * @param userId - 할당 해제 대상 사용자 식별자
	 * @param productName - 할당 해제 대상 라이선스 제품명
	 * @return 할당 해제 처리 결과
	 */
	@Transactional
	public boolean userLicenseRevokeRequestHandler(String workspaceId, String userId, String productName) {
		LicensePlan licensePlan = licensePlanRepository.findByWorkspaceIdAndPlanStatusNot(
			workspaceId, PlanStatus.TERMINATE
		).orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PLAN_NOT_FOUND));

		LicenseProduct licenseProduct = licenseProductRepository.findByLicensePlanAndProduct_Name(
			licensePlan, productName
		).orElseThrow(() -> new LicenseServiceException(ErrorCode.ERR_LICENSE_PRODUCT_NOT_FOUND));

		License currentUserLicense = licenseRepository.licenseAllocationRevokeByUserIdAndLicenseProductId(
			userId, licenseProduct.getId());

		if(currentUserLicense == null){
			log.info("Retrieve user license info via license plan : {} and license product: {} , but license info not found", licensePlan, licenseProduct);
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}

		// 제품 라이선스 상태가 만약 초과 상태인 경우, 라이선스 종료 상태로 표시
		if (licenseProduct.getStatus() == LicenseProductStatus.EXCEEDED) {
			currentUserLicense.setStatus(LicenseStatus.TERMINATE);
			licenseRepository.save(currentUserLicense);

			// 현재 사용중인 라이선스 갯수 계산
			long usedLicenseAmount = licenseProduct.getLicenseList()
				.stream()
				.filter(l -> l.getStatus().equals(LicenseStatus.USE))
				.count();

			// 제품 라이선스 갯수 범위 내에 들어온 경우
			if (usedLicenseAmount <= licenseProduct.getQuantity()) {
				// 제품 라이선스 초과 상태에서 정상 상태로 변경
				licenseProduct.setStatus(LicenseProductStatus.ACTIVE);
				licenseProductRepository.save(licenseProduct);
			}

		} else { // 초과 상태가 아닌 경우, 이전 사용자 할당 정보 제거 및 미사용 상태로 변경
			currentUserLicense.setUserId(null);
			currentUserLicense.setStatus(LicenseStatus.UNUSE);
			licenseRepository.save(currentUserLicense);
		}

		Map<Object, Object> pushContent = new HashMap<>();
		pushContent.put("productName", productName);
		LicenseExpiredEvent licenseExpiredEvent = new LicenseExpiredEvent(
			productName.toLowerCase(), workspaceId, userId, pushContent
		);
		log.info("[LICENSE DEALLOCATE_PUSH_MESSAGE_REQUEST] - {}", licenseExpiredEvent.toString());
		applicationEventPublisher.publishEvent(licenseExpiredEvent);

		return true;
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
			ApiResponse<WorkspaceInfoResponse> workspaceInfoResponseMessage = workspaceRestService.getWorkspaceInfo(
				detailsInfo.getWorkspaceId());
			WorkspaceInfoResponse workspaceInfoResponse = workspaceInfoResponseMessage.getData();
			if (workspaceInfoResponse.getUuid() == null || workspaceInfoResponse.getUuid().isEmpty()) {
				log.info("[WORKSPACE_NOT_FOUND] - {}", detailsInfo.toString());
				continue;
			}
			MyLicensePlanInfoResponse licensePlanInfoResponse = new MyLicensePlanInfoResponse();
			licensePlanInfoResponse.setWorkspaceId(workspaceInfoResponse.getUuid());
			licensePlanInfoResponse.setWorkspaceName(workspaceInfoResponse.getName());
			licensePlanInfoResponse.setPlanProduct(detailsInfo.getProductName());
			licensePlanInfoResponse.setRenewalDate(detailsInfo.getEndDate());
			licensePlanInfoResponse.setWorkspaceProfile(workspaceInfoResponse.getProfile());
			licensePlanInfoResponse.setProductPlanStatus(detailsInfo.getProductPlanStatus());
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

	/**
	 * 모든 라이선스 플랜 및 라이선스 관련 정보 비활성화 및 삭제
	 *
	 * @param workspaceUUID - 워크스페이스 식별자
	 * @param userUUID      - 사용자 식별자
	 * @param userNumber - 사용자 고유 식별자
	 * @return - 비활성화 및 삭제 결과
	 */
	@Transactional
	public LicenseSecessionResponse deleteAllLicenseInfo(String workspaceUUID, String userUUID, long userNumber) {
		LicensePlan licensePlan = licensePlanRepository.findByUserIdAndWorkspaceIdAndPlanStatus(
			userUUID, workspaceUUID, PlanStatus.ACTIVE
		);

		// 라이선스 플랜 정보가 없는 경우
		if (licensePlan == null) {
			log.info("workspaceUUID: {} , userUUID: {} ,  userNumber: {} ", workspaceUUID, userUUID, userNumber);
			log.info("[LICENSE_PLAN_SECESSION] - License plan not found.");
			return new LicenseSecessionResponse(workspaceUUID, true, LocalDateTime.now());
		}

		log.info("USER: {} , WORKSPACE: {}, LICENSE_PLAN_ID: {}", userUUID, workspaceUUID, licensePlan.getId());
		log.info("[LICENSE_PLAN_SECESSION] - {}", licensePlan.toString());

		// 정기 결제 내역 조회 및 취소
		payAPIService.billingCancelProcess(userNumber);

		// license product 정보 조회
		Set<LicenseProduct> licenseProductSet = licensePlan.getLicenseProductList();

		if (!licenseProductSet.isEmpty()) {
			log.info("[LICENSE_PLAN_SECESSION] - LICENSE_PRODUCT_INACTIVE BEGIN.");
			// license product 상태 inactive 로 변경
			licenseProductSet.forEach(lp -> lp.setStatus(LicenseProductStatus.INACTIVE));
			licenseProductRepository.saveAll(licenseProductSet);
			// license 할당 해제
			log.info(
				"[LICENSE_PLAN_SECESSION] - All license status changed to UNUSED and  delete user assigning information.");
			licenseRepository.updateAllLicenseInfoInactiveByLicenseProduct(licenseProductSet);
			log.info("[LICENSE_PLAN_SECESSION] - LICENSE_PRODUCT_INACTIVE END.");
		}

		// license plan 상태 비활성화 및 탈퇴 데이터 표시
		licensePlan.setPlanStatus(PlanStatus.TERMINATE);
		licensePlan.setModifiedUser(userUUID + "-" + "secession");
		licensePlanRepository.save(licensePlan);

		log.info(
			"[USER_SECESSION_LICENSE_PLAN_INACTIVE]: licensePlanId:{} , userId:{}, WorkspaceId: {}",
			licensePlan.getId(), licensePlan.getUserId(), licensePlan.getWorkspaceId()
		);

		return new LicenseSecessionResponse(workspaceUUID, true, LocalDateTime.now());
	}

}
