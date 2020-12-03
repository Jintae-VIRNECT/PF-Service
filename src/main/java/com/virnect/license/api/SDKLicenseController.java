package com.virnect.license.api;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

import com.virnect.license.application.SDKLicenseService;
import com.virnect.license.dto.request.SDKLicenseAuthenticationRequest;
import com.virnect.license.dto.request.SDKLicenseRegisterRequest;
import com.virnect.license.dto.response.SDKLicenseAuthenticationResponse;
import com.virnect.license.dto.response.SDKLicenseInfoListResponse;
import com.virnect.license.dto.response.SDKLicenseInfoResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageRequest;
import com.virnect.license.global.error.ErrorCode;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class SDKLicenseController {
	private static final String PARAMETER_LOG_MESSAGE = "[PARAMETER ERROR]:: {}";
	private final SDKLicenseService sdkLicenseService;

	@ApiOperation(value = "sdk 라이선스 발급")
	@PostMapping("/sdk")
	public ResponseEntity<ApiResponse<SDKLicenseInfoResponse>> sdkLicenseRegisterRequestHandler(
		@RequestBody SDKLicenseRegisterRequest licenseRegisterRequest, BindingResult result
	) {
		if (result.hasErrors()) {
			result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		SDKLicenseInfoResponse licenseRegisterResponse = sdkLicenseService.registerNewSdkLicense(
			licenseRegisterRequest);
		return ResponseEntity.ok(new ApiResponse<>(licenseRegisterResponse));
	}

	@ApiOperation(value = "sdk 라이선스 검증 API")
	@PostMapping("/sdk/authentication")
	public ResponseEntity<ApiResponse<SDKLicenseAuthenticationResponse>> sdkLicenseAuthenticationRequestHandler(
		@RequestBody SDKLicenseAuthenticationRequest licenseAuthenticationRequest, BindingResult result
	) {
		if (result.hasErrors()) {
			result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		SDKLicenseAuthenticationResponse authenticationResponse = sdkLicenseService.validateSerialKey(
			licenseAuthenticationRequest.getSerialKey());
		return ResponseEntity.ok(new ApiResponse<>(authenticationResponse));
	}

	@ApiOperation(value = "라이선스 목록 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "search", value = "검색할 시리얼 키(ALL: 전체조회)", dataType = "string", paramType = "query", defaultValue = "ALL"),
		@ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
		@ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호(1부터 시작)", paramType = "query", defaultValue = "1"),
		@ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "createdDate,desc"),
	})
	@GetMapping("/sdk/list")
	public ResponseEntity<ApiResponse<SDKLicenseInfoListResponse>> getSDKLicenseInfoList(
		@RequestParam(value = "search", defaultValue = "ALL") String search, @ApiIgnore PageRequest pageable
	) {
		SDKLicenseInfoListResponse sdkLicenseInfoListResponse = sdkLicenseService.getLicenseInfoList(search, pageable.of());
		return ResponseEntity.ok(new ApiResponse<>(sdkLicenseInfoListResponse));
	}
}
