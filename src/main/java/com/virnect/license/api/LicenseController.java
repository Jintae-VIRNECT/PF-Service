package com.virnect.license.api;

import javax.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

import com.virnect.license.application.LicenseService;
import com.virnect.license.dto.license.response.LicenseSecessionResponse;
import com.virnect.license.dto.license.response.MyLicenseInfoListResponse;
import com.virnect.license.dto.license.response.MyLicenseInfoResponse;
import com.virnect.license.dto.license.response.MyLicensePlanInfoListResponse;
import com.virnect.license.dto.license.response.WorkspaceLicensePlanInfoResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageRequest;
import com.virnect.license.global.error.ErrorCode;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description license related request controller
 * @since 2020.04.09
 */

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class LicenseController {
	private final LicenseService licenseService;

	@ApiOperation(value = "워크스페이스 라이선스 플랜 정보 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8"),
	})
	@GetMapping("/{workspaceId}/plan")
	public ResponseEntity<ApiResponse<WorkspaceLicensePlanInfoResponse>> getWorkspaceLicensePlanInfo(
		@PathVariable("workspaceId") String workspaceId
	) {
		if (!StringUtils.hasText(workspaceId)) {
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		// Todo: 필터 기능 추가
		WorkspaceLicensePlanInfoResponse responseMessage = licenseService.getWorkspaceLicensePlanInfo(workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}

	@ApiOperation(value = "워크스페이스에서 할당받은 내 라이선스 정보 조회")
	@GetMapping("/{workspaceId}/{userId}")
	public ResponseEntity<ApiResponse<MyLicenseInfoListResponse>> getMyLicenseInfoRequestHandler(
		@PathVariable("userId") @NotBlank String userId, @PathVariable("workspaceId")
	@NotBlank String workspaceId
	) {
		if (!StringUtils.hasText(userId)) {
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		// Todo: 페이징 및 필터 기능 추가
		ApiResponse<MyLicenseInfoListResponse> responseMessage = licenseService.getMyLicenseInfoList(
			userId,
			workspaceId
		);
		return ResponseEntity.ok(responseMessage);
	}

	@ApiOperation(value = "워크스페이스내에서 라이선스 할당")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", required = true),
		@ApiImplicitParam(name = "userId", value = "워크스페이스 유저 식별자", paramType = "path", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", required = true),
		@ApiImplicitParam(name = "productName", value = "제품명", paramType = "query", defaultValue = "make", required = true),
	})
	@PutMapping("/{workspaceId}/{userId}/grant")
	public ResponseEntity<ApiResponse<MyLicenseInfoResponse>> grantWorkspaceLicenseToUser(
		@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId,
		@RequestParam(value = "productName") String productName
	) {
		if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId) || !StringUtils.hasText(productName)) {
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		MyLicenseInfoResponse responseMessage = licenseService.userLicenseAllocateRequestHandler(
			workspaceId,
			userId,
			productName
		);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}

	@ApiOperation(value = "워크스페이스내에서 라이선스 할당 해제")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8"),
		@ApiImplicitParam(name = "userId", value = "워크스페이스 유저 식별자", paramType = "path", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608"),
		@ApiImplicitParam(name = "productName", value = "제품명", paramType = "query", defaultValue = "make"),
	})
	@PutMapping("/{workspaceId}/{userId}/revoke")
	public ResponseEntity<ApiResponse<Boolean>> revokeWorkspaceLicenseToUser(
		@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId,
		@RequestParam(value = "productName") String productName
	) {
		if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId) || !StringUtils.hasText(productName)) {
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		boolean responseMessage = licenseService.userLicenseRevokeRequestHandler(workspaceId, userId,
			productName
		);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}

	@ApiOperation(value = "사용중인 플랜 목록 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "userId", value = "사용자 식별자", paramType = "path", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608"),
		@ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
		@ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호(1부터 시작)", paramType = "query", defaultValue = "1"),
		@ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "renewalDate,desc")
	})
	@GetMapping("/plan/{userId}")
	public ResponseEntity<ApiResponse<MyLicensePlanInfoListResponse>> getMyLicensePlanInfoList(
		@PathVariable("userId") String userId, @ApiIgnore PageRequest pageRequest
	) {
		if (StringUtils.isEmpty(userId)) {
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		ApiResponse<MyLicensePlanInfoListResponse> responseMessage = licenseService.getMyLicensePlanInfoList(
			userId,
			pageRequest
		);
		return ResponseEntity.ok(responseMessage);
	}

	@ApiOperation(value = "라이선스 관련 정보 삭제 - 회원탈퇴", tags = "user server only")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceUUID", value = "워크스페이스 식별자", paramType = "path", example = "4d6eab0860969a50acbfa4599fbb5ae8"),
		@ApiImplicitParam(name = "userUUID", value = "사용자 식별자", paramType = "query", example = "4d6eab0860969a50acbfa4599fbb5ae8"),
		@ApiImplicitParam(name = "serviceID", value = "요청 서버 명", paramType = "header", example = "user-server")
	})
	@DeleteMapping("/secession/{workspaceUUID}")
	public ResponseEntity<ApiResponse<LicenseSecessionResponse>> licenseSecessionRequest(
		@PathVariable("workspaceUUID") String workspaceUUID,
		@RequestParam("userUUID") String userUUID,
		@RequestParam("userNumber") long userNumber,
		@RequestHeader("serviceID") String requestServiceID
	) {
		if (!StringUtils.hasText(workspaceUUID) || !StringUtils.hasText(userUUID) || !StringUtils.hasText(
			requestServiceID) || !requestServiceID.equals("user-server")) {
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		LicenseSecessionResponse responseMessage = licenseService.deleteAllLicenseInfo(
			workspaceUUID, userUUID, userNumber
		);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}
}

