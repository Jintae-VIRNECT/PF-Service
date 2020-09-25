package com.virnect.license.application;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.rest.billing.PayAPIService;
import com.virnect.license.application.rest.user.UserRestService;
import com.virnect.license.application.rest.workspace.WorkspaceRestService;
import com.virnect.license.dao.billing.LicenseAssignAuthInfoRepository;
import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.dao.licenseproduct.LicenseProductRepository;
import com.virnect.license.dao.product.ProductRepository;
import com.virnect.license.domain.billing.CouponPeriodType;
import com.virnect.license.domain.billing.LicenseAssignAuthInfo;
import com.virnect.license.domain.billing.ProductTypeId;
import com.virnect.license.domain.billing.ProductTypeName;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.licenseplan.PlanStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.LicenseProductStatus;
import com.virnect.license.domain.product.Product;
import com.virnect.license.dto.ResourceCalculate;
import com.virnect.license.dto.request.billing.AllocateCouponInfoResponse;
import com.virnect.license.dto.request.billing.AllocateProductInfoResponse;
import com.virnect.license.dto.request.billing.LicenseAllocateCheckRequest;
import com.virnect.license.dto.request.billing.LicenseProductAllocateRequest;
import com.virnect.license.dto.request.billing.LicenseProductDeallocateRequest;
import com.virnect.license.dto.request.billing.ProductTypeRequest;
import com.virnect.license.dto.response.LicenseProductAllocateCheckResponse;
import com.virnect.license.dto.response.LicenseProductDeallocateResponse;
import com.virnect.license.dto.response.biling.LicenseProductAllocateResponse;
import com.virnect.license.dto.rest.user.UserInfoRestResponse;
import com.virnect.license.dto.rest.user.WorkspaceInfoListResponse;
import com.virnect.license.dto.rest.user.WorkspaceInfoResponse;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {
	private static final long MAX_USER_AMOUNT = 50; // 9 명 -> 50명
	private static final long MAX_CALL_TIME = Long.MAX_VALUE; // (270 시간 -> 무제한)
	private static final long MAX_STORAGE_AMOUNT = 980000; // 180 기가
	private static final long MAX_DOWNLOAD_HITS = 149000; // 10만 회 + (49* 1000회) = 149000
	private static final int LICENSE_EXPIRED_HOUR = 23; // 오후 11시
	private static final int LICENSE_EXPIRED_MINUTE = 59; // 59분
	private static final int LICENSE_EXPIRED_SECONDS = 59; // 59초
	private static final long LICENSE_ASSIGN_AUTH_CODE_TTL_MINUTE = 30; // 30분간 지급 인증 코드 유효
	private static final long FIRST_WORKSPACE_DOWNLOAD_HITS = 100000; // 최초 워크스페이스 생성 시 다운로드  100000회 추가
	private static final String USER_REST_SERVICE_ERROR_LOG_FORMAT = "User service error response: [{}]";
	private final ProductRepository productRepository;
	private final UserRestService userRestService;
	private final WorkspaceRestService workspaceRestService;
	private final LicensePlanRepository licensePlanRepository;
	private final LicenseProductRepository licenseProductRepository;
	private final LicenseRepository licenseRepository;
	private final LicenseAssignAuthInfoRepository licenseAssignAuthInfoRepository;
	private final PayAPIService payAPIService;

	/**
	 * 상품 지급 여부 검사
	 *축
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

		log.info("[BILLING][REQUEST_TOTAL_RESOURCE] => {}", requestTotalResource.toString());

		// 3. 사용자의 현재 사용중인 라이선스 플랜 조회
		Optional<LicensePlan> licensePlan = licensePlanRepository.findByUserIdAndPlanStatus(
			requestUserInfo.getUuid(),
			PlanStatus.ACTIVE
		);

		// 3-1. 현재 기간 결제 라이선스 플랜이 활성화 되어있는 경우
		if (licensePlan.isPresent() && licensePlan.get().isTermPlan()) {
			List<HashMap<String, Object>> detailMessage = new ArrayList<>();
			HashMap<String, Object> rejectMessage = new HashMap<>();
			rejectMessage.put("type", "plan");
			rejectMessage.put("requestAmount", 0);
			rejectMessage.put("exceededAmount", 0);
			rejectMessage.put("availableAmount", 0);
			rejectMessage.put("message", "previous term plan is exist and activated.");
			detailMessage.add(rejectMessage);
			log.error("[BILLING_LICENSE_ALLOCATE_CHECK][ERROR_MESSAGE] - {}", detailMessage.toString());
			log.error("[BILLING_LICENSE_ALLOCATE_CHECK][PREVIOUS_PLAN] - {}", licensePlan.get().toString());
			throw new LicenseAllocateDeniedException(
				ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED, allocateCheckRequest.getUserId(), detailMessage
			);
		}

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

		// 3. 상품 지급 인증 정보 검증
		licenseAssignAuthInfoValidation(licenseAllocateRequest, licenseAssignAuthInfo, requestUserInfo);

		// 4. 지급 요청 상품의 서비스 총 이용량 계산
		ResourceCalculate resourceCalculate = calculateAllocateProductResource(licenseAllocateRequest.getProductList());

		// 4-1.지급 인증 정보 확인 - 통화 횟수, 용량, 다운로드 횟수
		licenseAllocatePropertyValidationCheck(licenseAssignAuthInfo, resourceCalculate);

		// 5. 지급 요청 사용자, 워크스페이스 정보 조회
		WorkspaceInfoResponse workspaceInfo = getWorkspaceInfoResponseByUserId(requestUserInfo.getUuid());

		// 6. 플랜 할당 이력 존재 유무 검사
		boolean hasAllocatedLicensePlan = licensePlanRepository.existsByUserIdAndPlanStatusIsIn(
			requestUserInfo.getUuid(), Arrays.asList(PlanStatus.ACTIVE, PlanStatus.INACTIVE)
		);

		log.info(
			"[BILLING][LICENSE_ALLOCATE] - User has active or inactive license plan check result: {}",
			hasAllocatedLicensePlan
		);

		// 7-1. 라이선스 할당 이력이 없는 경우 (신규 요청)
		if (!hasAllocatedLicensePlan) {
			// 라이선스 플랜 신규 지급
			LicenseProductAllocateResponse response = allocateNewLicensePlan(
				resourceCalculate, requestUserInfo, workspaceInfo, licenseAllocateRequest
			);
			return new ApiResponse<>(response);
		}

		// 8. 라이선스 플랜 정보 조회
		LicensePlan userLicensePlan = licensePlanRepository.findByUserIdAndWorkspaceIdAndPlanStatus(
			requestUserInfo.getUuid(), workspaceInfo.getUuid(), PlanStatus.ACTIVE);

		if (userLicensePlan == null) {
			log.error("[BILLING][LICENSE_ALLOCATE] - User License Plan (status = ACTIVE) Not found.");
			log.error("[BILLING][LICENSE_ALLOCATE] - {}", licenseAllocateRequest.toString());
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
		}

		// 8-1. 기존 활성화 되어있는 라이선스 플랜이 기간 결제인 경우
		// 해당 플랜이 만료되기전까지 갱신 불가
		if (userLicensePlan.isTermPlan()) {
			log.error("[BILLING][LICENSE_ALLOCATE] - Previous License plan is term payment plan and activated.");
			log.error("[BILLING][LICENSE_ALLOCATE][PLAN_INFO] - {}", userLicensePlan.toString());
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
		}

		log.info("[BILLING][name:{}, uuid: {}, email: {}] - 라이선스 플랜 갱신 작업 시작.",
			requestUserInfo.getName(), requestUserInfo.getUuid(), requestUserInfo.getEmail()
		);

		// 9. 상품 정보 변경 유무 확인(= 정기 결제 요청 유무 확인)
		boolean isRegularAllocateRequest = isRegularAllocateRequest(resourceCalculate, userLicensePlan);

		log.info("[BILLING][name:{}, uuid: {}, email: {}] - 정기 결제 요청 유무 : {} , 리소스 동일 여부: {}",
			requestUserInfo.getName(), requestUserInfo.getUuid(), requestUserInfo.getEmail(),
			licenseAllocateRequest.isRegularRequest(), isRegularAllocateRequest
		);

		// 10. 라이선스 상품 정보 수정 (추가 지급 or 축소 지급)
		if (!isRegularAllocateRequest) {
			renewalPreviousLicensePlan(licenseAllocateRequest, userLicensePlan);
		}

		// 라이선스 플랜 리소스 정보 및 만료 기간 갱신 프로세스
		userLicensePlan.setMaxCallTime(resourceCalculate.getTotalCallTime());
		userLicensePlan.setMaxStorageSize(resourceCalculate.getTotalStorageSize());

		// 다운로드 횟수는 최초 워크스페이스 생성 시 지급된 횟수보다 높은 경우에만 갱신
		if (resourceCalculate.getTotalDownloadHit() > FIRST_WORKSPACE_DOWNLOAD_HITS) {
			userLicensePlan.setMaxDownloadHit(resourceCalculate.getTotalDownloadHit());
		}
		userLicensePlan.setPaymentId(licenseAllocateRequest.getPaymentId());
		userLicensePlan.setCountryCode(licenseAllocateRequest.getUserCountryCode());
		userLicensePlan.setEndDate(userLicensePlan.getEndDate().plusMonths(1));
		licensePlanRepository.save(userLicensePlan);

		// 라이선스 지급 인증 정보 삭제
		licenseAssignAuthInfoRepository.deleteById(licenseAllocateRequest.getAssignAuthCode());

		LicenseProductAllocateResponse response = LicenseProductAllocateResponse.builder()
			.userId(licenseAllocateRequest.getUserId())
			.paymentId(licenseAllocateRequest.getPaymentId())
			.allocatedProductList(licenseAllocateRequest.getProductList())
			.allocatedDate(LocalDateTime.now())
			.build();
		return new ApiResponse<>(response);
	}

	/**
	 *  최초 라이선스 플랜 지급 처리
	 * @param resourceCalculate - 상품 지급 리소스 용량 정보
	 * @param requestUserInfo - 지급 요청 사용자 정보
	 * @param workspaceInfo - 지급 요청 사용자의 마스터 워크스페이스 정보
	 * @param licenseAllocateRequest - 라이선스 할당 요청
	 * @return 라이선스 플랜 생성 정보
	 */
	private LicenseProductAllocateResponse allocateNewLicensePlan(
		ResourceCalculate resourceCalculate,
		UserInfoRestResponse requestUserInfo,
		WorkspaceInfoResponse workspaceInfo,
		LicenseProductAllocateRequest licenseAllocateRequest
	) {
		log.info("name:{}, uuid: {}, email: {}, 라이선스 플랜 신규 생성 작업 시작.",
			requestUserInfo.getName(), requestUserInfo.getUuid(), requestUserInfo.getEmail()
		);

		// 1개월 무료 이벤트 쿠폰 사용 여부 확인,
		boolean isEventPlan = licenseAllocateRequest.getCouponList()
			.stream()
			.allMatch(c -> c.getCouponName().contains("1개월 무료 사용 할인 쿠폰"));

		// 구독 결제 라이선스 만료 기간 설정 (결제 시점으로 부터 한달 뒤 자정)
		LocalDateTime expiredDate = LocalDate.now().plusDays(30)
			.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);

		// 기간 결제 요청인 경우
		if (licenseAllocateRequest.isTermPaymentRequest()) {
			// 기간 결제 요청일 때 100% 할인 쿠폰 정보가 없는 경우 예외 발생
			AllocateCouponInfoResponse freeCouponInfo = Optional.ofNullable(
				licenseAllocateRequest.getCouponList().get(0)).orElseThrow(() -> {
				log.error(
					"[BILLING][LICENSE_ALLOCATE_TERM_PAYMENT] - Term Payment request fail. Coupon Information Not Found.");
				log.error("[BILLING][LICENSE_ALLOCATE_TERM_PAYMENT] - {}", licenseAllocateRequest.toString());
				return new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
			});

			// 라이선스 만료 기간 계산 및 기존 구독 결제 기반 라이선스 만료일 정보를 기간 결제 만료일로 갱신
			if (freeCouponInfo.getPeriodType().equals(CouponPeriodType.DAY)) { // 일 할인인 경우
				expiredDate = LocalDate.now().plusDays(freeCouponInfo.getPeriod())
					.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);
			} else if (freeCouponInfo.getPeriodType().equals(CouponPeriodType.MONTH)) { // 월 할인인 경우
				expiredDate = LocalDate.now().plusMonths(freeCouponInfo.getPeriod())
					.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);
			} else if (freeCouponInfo.getPeriodType().equals(CouponPeriodType.YEAR)) { // 연 할인인 경우
				expiredDate = LocalDate.now().plusYears(freeCouponInfo.getPeriod())
					.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);
			}
		}

		// 11. 최초 구매, 라이선스 플랜 생성
		LicensePlan licensePlan = LicensePlan.builder()
			.userId(requestUserInfo.getUuid())
			.workspaceId(workspaceInfo.getUuid())
			.startDate(LocalDateTime.now())
			.endDate(expiredDate)
			.planStatus(PlanStatus.ACTIVE)
			.maxCallTime(resourceCalculate.getTotalCallTime())
			.maxStorageSize(resourceCalculate.getTotalStorageSize())
			.maxDownloadHit(FIRST_WORKSPACE_DOWNLOAD_HITS)
			.maxUserAmount(MAX_USER_AMOUNT)
			.paymentId(licenseAllocateRequest.getPaymentId())
			.countryCode(licenseAllocateRequest.getUserCountryCode())
			.isEventPlan(isEventPlan)
			.isTermPlan(licenseAllocateRequest.isTermPaymentRequest())
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

		log.info("name:{}, uuid: {}, email: {}, 라이선스 플랜 신규 생성 작업 종료.",
			requestUserInfo.getName(), requestUserInfo.getUuid(), requestUserInfo.getEmail()
		);
		log.info("name:{}, uuid: {}, email: {} :: {}",
			requestUserInfo.getName(), requestUserInfo.getUuid(), requestUserInfo.getEmail(), licensePlan.toString()
		);
		return allocateResponse;
	}

	/**
	 * 라이선스 플랜 갱신 지급 처리
	 * @param licenseAllocateRequest - 라이선스 할당 요청
	 * @param licensePlan - 기존 라이선스 플랜 정보
	 */
	private void renewalPreviousLicensePlan(
		LicenseProductAllocateRequest licenseAllocateRequest,
		LicensePlan licensePlan
	) {
		List<AllocateProductInfoResponse> allocateProductInfoList = licenseAllocateRequest.getProductList();
		for (AllocateProductInfoResponse allocateProduct : allocateProductInfoList) {
			Product product = productRepository.findByBillProductId(allocateProduct.getProductId())
				.orElseThrow(
					() -> {
						log.error(
							"[BILLING][PRODUCT_NOT_FOUND] -> planId: {} allocateProductInfo: {}",
							licensePlan.getId(), allocateProduct.toString()
						);
						return new BillingServiceException(
							ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
					}
				);

			Optional<LicenseProduct> licenseProductInfo = licenseProductRepository.findByLicensePlanAndProduct(
				licensePlan, product
			);

			if (licenseProductInfo.isPresent()) {
				// 기존 상품 정보 수정
				LicenseProduct originLicenseProduct = licenseProductInfo.get();

				// 라이선스 상품 추가 지급 요청
				if (originLicenseProduct.getQuantity() < allocateProduct.getProductAmount()) {
					log.info(
						"[BILLING][INCREASE_PRODUCT] - [{}] is {} to {}", allocateProduct.toString(),
						originLicenseProduct.getQuantity(), allocateProduct.getProductAmount()
					);
					originLicenseProductResourceUpdate(allocateProduct, originLicenseProduct);
					originLicenseProduct.setStatus(LicenseProductStatus.ACTIVE);
					licenseProductRepository.save(originLicenseProduct);
					assignLicenseProductMoreThanOriginLicensePlan(allocateProduct, originLicenseProduct);
				}

				// 라이선스 상품 축소 지급 요청 (기존 제품 라이선스 수 > 할당 요청의 제품 라이선스 수)
				if (originLicenseProduct.getQuantity() > allocateProduct.getProductAmount()) {
					log.info(
						"[BILLING][DECREASE_LICENSE_PRODUCT] - [{}] is {} to {}", allocateProduct.toString(),
						originLicenseProduct.getQuantity(), allocateProduct.getProductAmount()
					);
					// 기존 제품 라이선스 리소스 정보를 할당 요청의 리소스 정보로 수정
					originLicenseProductResourceUpdate(allocateProduct, originLicenseProduct);

					// 실 사용 라이선스 갯수 계산
					long usedLicenseAmount = licenseRepository.countByLicenseProductAndStatus(
						originLicenseProduct, LicenseStatus.USE);

					// 실 사용 라이선스 갯수 > 할당 요청된 라이선스 갯수 = 라이선스 갯수 초과 표시
					if (allocateProduct.getProductType().getId().equals(ProductTypeId.PRODUCT.getValue()) &&
						allocateProduct.getProductAmount() > usedLicenseAmount) {
						originLicenseProduct.setStatus(LicenseProductStatus.EXCEEDED);

						// TODO: 2020-09-04 기 할당 라이선스 초과 상황에서, 기존 제품 라이선스 중 미 사용중인 라이선스 제거 로직 추가
					}
					// TODO: 2020-09-04 기 할당 라이선스 축소 상황에서, 축소범위에 맞춰 미 사용중인 라이선스 제거 로직 추가

					licenseProductRepository.save(originLicenseProduct);
				}
			} else {
				// 새 라이선스 상품 추가 케이스
				registerNewLicenseProduct(licensePlan, allocateProduct, product);
			}
		}
	}

	/**
	 * 기존 라이선스 제품 리소스 정보 갱신
	 * @param allocateProduct - 라이선스 제품 할당 요청 정보
	 * @param originLicenseProduct - 기존 라이선스 제품 정보
	 */
	private void originLicenseProductResourceUpdate(
		AllocateProductInfoResponse allocateProduct, LicenseProduct originLicenseProduct
	) {
		// 해당 라이선스 상품의 통화 시간 정보 변경
		originLicenseProduct.setCallTime(
			allocateProduct.getProductCallTime() * allocateProduct.getProductAmount()
		);
		// 해당 라이선스 상품의 용량 정보 변경
		originLicenseProduct.setStorageSize(
			allocateProduct.getProductStorage() * allocateProduct.getProductAmount()
		);
		// 해당 라이선스 상품의 다운로드 횟수 정보 변경
		originLicenseProduct.setDownloadHit(
			allocateProduct.getProductHit() * allocateProduct.getProductAmount()
		);
		// 해당 라이선스 상품의 수량 정보 변경
		originLicenseProduct.setQuantity(allocateProduct.getProductAmount());
		log.info("[ORIGIN LICENSE PRODUCT UPDATE] -> {} to {}",
			originLicenseProduct.toString(), allocateProduct.toString()
		);
	}

	/**
	 * 라이선스 상품 축소 처리
	 * @param decreaseLicenseProductInfo - 축소 되는 라이선스 제품 요청 정보
	 * @param licensePlan - 기존 라이선스 플랜 정보
	 */
	private void assignLicenseProductLessThanOriginLicensePlan(
		AllocateProductInfoResponse decreaseLicenseProductInfo, LicensePlan licensePlan
	) {
		Product product = productRepository.findByBillProductId(decreaseLicenseProductInfo.getProductId())
			.orElseThrow(
				() -> {
					log.error(
						"[PRODUCT NOT FOUND] -> planId: {} productId: {}", licensePlan.getId(),
						decreaseLicenseProductInfo.getProductId()
					);
					log.error("[DECREASE LICENSE PRODUCT INFO] -> {}", decreaseLicenseProductInfo.toString());
					return new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
				}
			);
		LicenseProduct licenseProduct = licenseProductRepository.findByLicensePlanAndProduct(licensePlan, product)
			.orElseThrow(
				() -> {
					log.error(
						"[LICENSE PRODUCT NOT FOUND] -> planId: {} productId: {}", licensePlan.getId(),
						decreaseLicenseProductInfo.getProductId()
					);
					log.error("[DECREASE LICENSE PRODUCT INFO] -> {}", decreaseLicenseProductInfo.toString());
					return new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
				}
			);
		// 축소된 라이선스 갯수 할당		
		licenseProduct.setQuantity(decreaseLicenseProductInfo.getProductAmount());
		// 라이선스 초과 상태로 변경
		licenseProduct.setStatus(LicenseProductStatus.EXCEEDED);
		// 변경 상태 저장
		licenseProductRepository.save(licenseProduct);
	}

	/**
	 * 라이선스 상품 추가 지급 처리
	 * @param increaseLicenseProductInfo - 라이선스 할당 요청 정보
	 * @param licenseProduct - 기존 라이선스 제품 정보
	 */
	private void assignLicenseProductMoreThanOriginLicensePlan(
		AllocateProductInfoResponse increaseLicenseProductInfo, LicenseProduct licenseProduct
	) {
		// 추가 지급 요청 상품의 수량(추가 수량 + 이전 제품 수량) - 현재 상품의 수량 = 추가 수량
		int newLicenseProductAmount = increaseLicenseProductInfo.getProductAmount() - licenseProduct.getQuantity();
		licenseGenerateAndRegisterByLicenseProduct(licenseProduct, newLicenseProductAmount);
	}

	/**
	 * 마스터 워크스페이스 정보 추출
	 * @param userUUID - 사용자 고유 식별자
	 * @return - 마스터 워크스페이스 정보
	 */
	private WorkspaceInfoResponse getWorkspaceInfoResponseByUserId(String userUUID) {
		ApiResponse<WorkspaceInfoListResponse> workspaceApiResponse = workspaceRestService.getMyWorkspaceInfoList(
			userUUID, 50
		);

		if (workspaceApiResponse.getCode() != 200 || workspaceApiResponse.getData().getWorkspaceList() == null) {
			log.error(USER_REST_SERVICE_ERROR_LOG_FORMAT, workspaceApiResponse.getMessage());
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
		}
		return workspaceApiResponse.getData().getWorkspaceList()
			.stream()
			.filter(w -> w.getRole().equals("MASTER")).findFirst()
			.orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_ALLOCATE_DENIED));
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
		log.info("[BILLING][LICENSE_PLAN_REFUND_REQUEST] - {}", licenseDeallocateRequest.toString());

		// 1. 계정 정보 조회
		UserInfoRestResponse requestUserInfo = getUserInfoRestResponseByUserId(
			licenseDeallocateRequest.getUserId(), ErrorCode.ERR_BILLING_LICENSE_DEALLOCATE_PLAN_NOT_FOUND
		);

		// 2. 마스터 워크스페이스 정보 조회
		WorkspaceInfoResponse requestUserMasterWorkspaceInfo = getWorkspaceInfoResponseByUserId(
			requestUserInfo.getUuid());

		// 3. 라이선스 플랜 정보 조회
		LicensePlan licensePlan = licensePlanRepository.findByUserIdAndWorkspaceIdAndPlanStatus(
			requestUserInfo.getUuid(), requestUserMasterWorkspaceInfo.getUuid(), PlanStatus.ACTIVE
		);

		// 3-1. 활성화된 라이선스 플랜이 없는 경우
		if (licensePlan == null) {
			log.error("[BILLING][LICENSE_PLAN_REFUND] - active license plan not found");
			throw new BillingServiceException(ErrorCode.ERR_BILLING_LICENSE_DEALLOCATE_PLAN_NOT_FOUND);
		}

		// 4. license product 정보 조회
		Set<LicenseProduct> licenseProductSet = licensePlan.getLicenseProductList();

		if (!licenseProductSet.isEmpty()) {
			log.info("[BILLING][LICENSE_PLAN_REFUND] - LICENSE_PRODUCT_INACTIVE BEGIN.");
			// license product 상태 inactive 로 변경
			licenseProductSet.forEach(lp -> lp.setStatus(LicenseProductStatus.INACTIVE));
			licenseProductRepository.saveAll(licenseProductSet);
			// license 할당 해제
			log.info(
				"[BILLING][LICENSE_PLAN_REFUND] - All license status changed to UNUSED and  delete user assigning information.");
			licenseRepository.updateAllLicenseInfoInactiveByLicenseProduct(licenseProductSet);
			log.info("[BILLING][LICENSE_PLAN_REFUND] - LICENSE_PRODUCT_INACTIVE END.");
		}

		// 5. license plan 상태 비활성화 및 환불 데이터 표시
		licensePlan.setPlanStatus(PlanStatus.TERMINATE);
		licensePlan.setModifiedUser(requestUserInfo.getUuid() + "-" + "refund");
		licensePlanRepository.save(licensePlan);

		// 6. 정기 결제 내역 조회 및 정기 결제 취소 처리
		payAPIService.billingCancelProcess(licenseDeallocateRequest.getUserId());

		// 7. 라이선스 할당 해제 응답 데이터 생성
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
	 * @param resourceCalculate  - 지급 요청의 리소스 양 정보
	 */
	private void licenseAllocatePropertyValidationCheck(
		LicenseAssignAuthInfo licenseAssignAuthInfo, ResourceCalculate resourceCalculate
	) {
		if (!licenseAssignAuthInfo.getTotalProductCallTime().equals(resourceCalculate.getTotalCallTime())
			|| !licenseAssignAuthInfo.getTotalProductHit().equals(resourceCalculate.getTotalDownloadHit())
			|| !licenseAssignAuthInfo.getTotalProductStorage().equals(resourceCalculate.getTotalStorageSize())) {
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_AUTHENTICATION_CODE);
		}
	}

	/**
	 * 할당 요청 상품 정보 기반으로 등록
	 *
	 * @param allocateProductList - 지급 상품 정보 리스트
	 * @param licensePlan         - 상품 지급 대상 라이선스 플랜 정보
	 */
	private void licenseRegisterByProduct(
		List<AllocateProductInfoResponse> allocateProductList, LicensePlan licensePlan
	) {
		if (allocateProductList == null || allocateProductList.isEmpty()) {
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
		}
		allocateProductList.forEach(productInfo -> {
			Product product = productRepository.findByBillProductId(productInfo.getProductId())
				.orElseThrow(() -> {
					log.error("ASSIGN REQUEST PRODUCT NOT FOUND -> [{}] ", productInfo.toString());
					return new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT);
				});
			registerNewLicenseProduct(licensePlan, productInfo, product);
		});
	}

	/**
	 * 새 라이선스 상품 추가
	 * @param licensePlan - 라이선스 플랜 정보
	 * @param productInfo - 추가 요청 라이선스 상품 정보
	 * @param product - 상품 정보
	 */
	private void registerNewLicenseProduct(
		LicensePlan licensePlan, AllocateProductInfoResponse productInfo, Product product
	) {
		// 새 라이선스 상품 정보 생성
		LicenseProduct licenseProduct = LicenseProduct.builder()
			.product(product)
			.licensePlan(licensePlan)
			.callTime(productInfo.getProductCallTime() * productInfo.getProductAmount())
			.storageSize(productInfo.getProductStorage() * productInfo.getProductAmount())
			.downloadHit(productInfo.getProductHit() * productInfo.getProductAmount())
			.quantity(productInfo.getProductAmount())
			.build();
		licenseProductRepository.save(licenseProduct);
		log.info("[BILLING][NEW_LICENSE_PRODUCT_GENERATE] - {}", licenseProduct.toString());
		// 상품일때만 라이선스 생성
		if (productInfo.getProductType().getId().equals(ProductTypeId.PRODUCT.getValue())) {
			licenseGenerateAndRegisterByLicenseProduct(licenseProduct, productInfo.getProductAmount());
		}
	}

	/**
	 * 새 라이선스 생성
	 *
	 * @param licenseProduct - 제품 라이선스 정보
	 * @param quantity - 수량
	 */
	private void licenseGenerateAndRegisterByLicenseProduct(LicenseProduct licenseProduct, int quantity) {
		for (int i = 0; i < quantity; i++) {
			License license = License.builder()
				.status(LicenseStatus.UNUSE)
				.serialKey(UUID.randomUUID().toString().toUpperCase())
				.licenseProduct(licenseProduct)
				.build();
			licenseRepository.save(license);
			log.info("[BILLING][NEW_PRODUCT_LICENSE_GENERATE] - {}", license.toString());
		}
	}

	/**
	 * 할당 요청 상품별 서비스 이용량 계산 처리
	 *
	 * @param allocateProductList - 할당 요청 상품 정보 리스트
	 * @return 상품별 서비스 이용량 총합 정보
	 */
	private ResourceCalculate calculateAllocateProductResource(
		List<AllocateProductInfoResponse> allocateProductList
	) {
		long calculateMaxCallTime = 0;
		long calculateMaxStorage = 0;
		long calculateMaxHit = 0;
		for (AllocateProductInfoResponse product : allocateProductList) {
			ProductTypeRequest productTypeInfo = product.getProductType();
			String productTypeName = productTypeInfo.getName();
			if (ProductTypeName.REMOTE.is(productTypeName) || ProductTypeName.CALL_TIME.is(productTypeName)) {
				calculateMaxCallTime += product.getProductCallTime() * product.getProductAmount();
			} else if (ProductTypeName.MAKE.is(productTypeName) || ProductTypeName.STORAGE.is(productTypeName)) {
				calculateMaxStorage += product.getProductStorage() * product.getProductAmount();
			} else if (ProductTypeName.VIEW.is(productTypeName) || ProductTypeName.HIT.is(productTypeName)) {
				calculateMaxHit += product.getProductHit() * product.getProductAmount();
			}
		}
		log.info("callTime: {} , storageSize: {}, Hit: {}", calculateMaxCallTime, calculateMaxStorage, calculateMaxHit);
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

	/**
	 * 정기 결제 제품 할당 요청 여부 검사
	 * @param resourceCalculate - 요청 리소스 계산 정보
	 * @param licensePlan - 이전 라이선스 플랜 정보
	 * @return true=정기요청, false=축소 또는 추가 요청
	 */
	private boolean isRegularAllocateRequest(
		ResourceCalculate resourceCalculate, LicensePlan licensePlan
	) {
		return resourceCalculate.getTotalCallTime() == licensePlan.getMaxCallTime()
			& resourceCalculate.getTotalStorageSize() == licensePlan.getMaxStorageSize()
			& resourceCalculate.getTotalDownloadHit() == licensePlan.getMaxDownloadHit();
	}
}
