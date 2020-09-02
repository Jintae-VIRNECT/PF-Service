package com.virnect.license.api;

import javax.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.BillingService;
import com.virnect.license.dto.request.billing.LicenseAllocateCheckRequest;
import com.virnect.license.dto.request.billing.LicenseProductAllocateRequest;
import com.virnect.license.dto.request.billing.LicenseProductDeallocateRequest;
import com.virnect.license.dto.response.LicenseProductAllocateCheckResponse;
import com.virnect.license.dto.response.LicenseProductDeallocateResponse;
import com.virnect.license.dto.response.biling.LicenseProductAllocateResponse;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.error.ErrorCode;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class BillingController {
	private static final String PARAMETER_LOG_MESSAGE = "[BILLING_CONTROLLER][PARAMETER ERROR]:: {}";
	private final BillingService billingService;

	@ApiOperation(value = "상품 지급")
	@PostMapping("/allocate")
	public ApiResponse<LicenseProductAllocateResponse> licenseProductAllocateToUser(
		@RequestBody @Valid LicenseProductAllocateRequest licensePRoductAllocateRequest, BindingResult result
	) {
		if (result.hasErrors()) {
			result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
			throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		return this.billingService.licenseAllocateRequest(licensePRoductAllocateRequest);
	}

	@ApiOperation(value = "상품 지급 취소")
	@PostMapping("/deallocate")
	public ApiResponse<LicenseProductDeallocateResponse> licenseProductDeallocateToUser(
		@RequestBody @Valid LicenseProductDeallocateRequest licenseDeallocateRequest, BindingResult result
	) {
		if (result.hasErrors()) {
			result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
			throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		return this.billingService.licenseDeallocateRequest(licenseDeallocateRequest);
	}

	@ApiOperation(value = "상품 지급 가능 여부 조회")
	@PostMapping("/allocate/check")
	public ApiResponse<LicenseProductAllocateCheckResponse> licenseAllocateCheckRequest(
		@RequestBody @Valid LicenseAllocateCheckRequest allocateCheckRequest, BindingResult result
	) {
		if (result.hasErrors()) {
			result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
			throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		return this.billingService.licenseAllocateCheckRequest(allocateCheckRequest);
	}
}
