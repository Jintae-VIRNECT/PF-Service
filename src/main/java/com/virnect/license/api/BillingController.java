package com.virnect.license.api;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.BillingService;

@Api
@Slf4j
@Profile(value = "!onpremise")
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class BillingController {
	private static final String PARAMETER_LOG_MESSAGE = "[BILLING_CONTROLLER][PARAMETER ERROR]:: {}";
	private final BillingService billingService;

	/**
	 * @deprecated - 페이레터 서비스를 정지함에 따라 API를 사용하지 않기에 주석처리함.
	 * 추후 페이레터 서비스를 다시 사용할 경우가 있을 수 있어 주석처리함.
	 */
	// @ApiOperation(value = "상품 지급")
	// @PostMapping("/allocate")
	// public ApiResponse<LicenseProductAllocateResponse> licenseProductAllocateToUser(
	// 	@RequestBody @Valid LicenseProductAllocateRequest licensePRoductAllocateRequest, BindingResult result
	// ) {
	// 	if (result.hasErrors()) {
	// 		result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
	// 		throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
	// 	}
	// 	return billingService.licenseAllocateRequest(licensePRoductAllocateRequest);
	// }
	//
	// @ApiOperation(value = "상품 지급 취소")
	// @PostMapping("/deallocate")
	// public ApiResponse<LicenseProductDeallocateResponse> licenseProductDeallocateToUser(
	// 	@RequestBody @Valid LicenseProductDeallocateRequest licenseDeallocateRequest, BindingResult result
	// ) {
	// 	if (result.hasErrors()) {
	// 		result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
	// 		throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
	// 	}
	// 	return billingService.licenseDeallocateRequest(licenseDeallocateRequest);
	// }
	//
	// @ApiOperation(value = "상품 지급 가능 여부 조회")
	// @PostMapping("/allocate/check")
	// public ApiResponse<LicenseProductAllocateCheckResponse> licenseAllocateCheckRequest(
	// 	@RequestBody @Valid LicenseAllocateCheckRequest allocateCheckRequest, BindingResult result
	// ) {
	// 	if (result.hasErrors()) {
	// 		result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
	// 		throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
	// 	}
	// 	return billingService.licenseAllocateCheckRequest(allocateCheckRequest);
	// }
}
