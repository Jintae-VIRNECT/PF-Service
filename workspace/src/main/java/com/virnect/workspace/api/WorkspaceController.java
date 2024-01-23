package com.virnect.workspace.api;

import java.util.Locale;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

import com.virnect.workspace.application.workspace.WorkspaceService;
import com.virnect.workspace.dto.request.WorkspaceCreateRequest;
import com.virnect.workspace.dto.request.WorkspaceFaviconUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceLogoUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceRemoteLogoUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceTitleUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceUpdateRequest;
import com.virnect.workspace.dto.response.WorkspaceCustomSettingResponse;
import com.virnect.workspace.dto.response.WorkspaceFaviconUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoDTO;
import com.virnect.workspace.dto.response.WorkspaceInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceLicenseInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceLogoUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceRemoteLogoUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceSecessionResponse;
import com.virnect.workspace.dto.response.WorkspaceTitleUpdateResponse;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.PageRequest;
import com.virnect.workspace.global.error.ErrorCode;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-08
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: workspace service rest api controller
 */
@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkspaceController {
	private final WorkspaceService workspaceService;

	@ApiOperation(
		value = "워크스페이스 시작하기",
		notes = "워크스페이스를 생성하는 기능입니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "userId", value = " 유저 uuid", dataType = "string", paramType = "form", defaultValue = "uuid", required = true),
		@ApiImplicitParam(name = "name", value = "워크스페이스 이름(빈값 일 경우 닉네임's Workspace로 저장됩니다.)", dataType = "string", paramType = "form", defaultValue = "USER's Workspace", required = true),
		@ApiImplicitParam(name = "profile", value = "워크스페이스 프로필", dataType = "__file", paramType = "form"),
		@ApiImplicitParam(name = "description", value = "워크스페이스 설명", dataType = "string", paramType = "form", defaultValue = "워크스페이스 입니다.", required = true)
	})
	@PostMapping
	public ResponseEntity<ApiResponse<WorkspaceInfoDTO>> createWorkspace(
		@ModelAttribute @Valid WorkspaceCreateRequest workspaceCreateRequest, BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[CREATE WORKSPACE INFO] Parameter Error Message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceInfoDTO response = workspaceService.createWorkspace(workspaceCreateRequest);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(
		value = "워크스페이스 프로필 설정",
		notes = "생성된 워크스페이스의 프로필을 변경하는 기능입니다.(마스터 유저만 가능한 기능입니다.)"
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", paramType = "form", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", required = true),
		@ApiImplicitParam(name = "userId", value = "마스터 유저 uuid", dataType = "string", paramType = "form", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", required = true),
		@ApiImplicitParam(name = "name", value = "워크스페이스 이름", dataType = "string", paramType = "form", defaultValue = "USER's Workspace", required = true),
		@ApiImplicitParam(name = "profile", value = "워크스페이스 프로필", dataType = "__file", paramType = "form"),
		@ApiImplicitParam(name = "description", value = "워크스페이스 설명", dataType = "string", paramType = "form", defaultValue = "워크스페이스 입니다.", required = true)
	})
	@PutMapping
	public ResponseEntity<ApiResponse<WorkspaceInfoDTO>> setWorkspace(
		@ModelAttribute @Valid WorkspaceUpdateRequest workspaceUpdateRequest, BindingResult bindingResult,
		@Validated @ApiIgnore Locale locale
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[UPDATE WORKSPACE INFO] Parameter Error Message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceInfoDTO response = workspaceService.setWorkspace(workspaceUpdateRequest, locale);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(
		value = "내가 속한 워크스페이스 목록 조회",
		notes = "사용자가 마스터, 매니저, 멤버로 소속되어 있는 워크스페이스 정보를 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "userId", value = "유저 uuid", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", required = true),
		@ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호", paramType = "query", defaultValue = "0"),
		@ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
		@ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "createdDate,desc"),
	})
	@GetMapping
	public ResponseEntity<ApiResponse<WorkspaceInfoListResponse>> getUserWorkspaces(
		@RequestParam("userId") String userId, @ApiIgnore PageRequest pageRequest
	) {
		if (!StringUtils.hasText(userId)) {
			log.error("[GET MY WORKSPACE INFO] Parameter Error Message : userId=[{}]", userId);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceInfoListResponse workspaceInfoListResponse = workspaceService.getUserWorkspaces(userId, pageRequest);
		return ResponseEntity.ok(new ApiResponse<>(workspaceInfoListResponse));
	}

	@ApiOperation(
		value = "워크스페이스 상세 정보 조회",
		notes = "워크스페이스 홈에서 워크스페이스의 정보를 조회합니다."
	)
	@GetMapping("/home/{workspaceId}")
	public ResponseEntity<ApiResponse<WorkspaceInfoResponse>> getWorkspaceDetailInfo(
		@PathVariable("workspaceId") String workspaceId
	) {
		if (!StringUtils.hasText(workspaceId)) {
			log.error("[GET DETAIL WORKSPACE INFO] Parameter Error Message : workspaceId=[{}]", workspaceId);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceInfoResponse response = workspaceService.getWorkspaceDetailInfo(workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(
		value = "워크스페이스 라이선스 조회",
		notes = "워크스페이스 라이선스 정보를 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	})
	@GetMapping("/{workspaceId}/license")
	public ResponseEntity<ApiResponse<WorkspaceLicenseInfoResponse>> getWorkspaceLicenseInfo(
		@PathVariable("workspaceId") String workspaceId
	) {
		if (!StringUtils.hasText(workspaceId)) {
			log.error("[GET WORKSPACE LICENSE INFO] Parameter Error Message : workspaceId=[{}]", workspaceId);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceLicenseInfoResponse response = workspaceService.getWorkspaceLicenseInfo(workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 정보 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", example = "4d6eab0860969a50acbfa4599fbb5ae8")
	})
	@GetMapping("/{workspaceId}/info")
	public ResponseEntity<ApiResponse<WorkspaceInfoDTO>> getWorkspaceInfo(
		@PathVariable("workspaceId") String workspaceId
	) {
		if (!StringUtils.hasText(workspaceId)) {
			log.error("[GET WORKSPACE INFO] Parameter Error Message : workspaceId=[{}]", workspaceId);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceInfoDTO response = workspaceService.getWorkspaceInfo(workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 관련 정보 삭제 - 회원탈퇴", tags = "user-server-only-controller")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "userUUID", value = "유저의 식별자", paramType = "path", example = "LtvGcPoq0WUFv"),
		@ApiImplicitParam(name = "serviceID", value = "요청 서버 명", paramType = "header", example = "user-server")
	})
	@DeleteMapping("/secession/{userUUID}")
	public ResponseEntity<ApiResponse<WorkspaceSecessionResponse>> workspaceSecessionRequest(
		@PathVariable("userUUID") String userUUID, @RequestHeader("serviceID") String requestServiceID
	) {
		if (!StringUtils.hasText(userUUID) || !StringUtils.hasText(requestServiceID) || !requestServiceID.equals(
			"user-server")) {
			log.error(
				"[DELETE WORKSPACE INFO] Parameter Error Message : userUUID=[{}], requestServiceID=[{}]", userUUID,
				requestServiceID
			);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceSecessionResponse responseMessage = workspaceService.deleteAllWorkspaceInfo(userUUID);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}

	@ApiOperation(value = "워크스페이스 고객사명 변경")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
	})
	@PostMapping("/{workspaceId}/title")
	public ResponseEntity<ApiResponse<WorkspaceTitleUpdateResponse>> updateWorkspaceTitle(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid WorkspaceTitleUpdateRequest workspaceTitleUpdateRequest, BindingResult bindingResult
	) {
		log.info(
			"[UPDATE_WORKSPACE_TITLE] WORKSPACE : {}, REQ : {}", workspaceId, workspaceTitleUpdateRequest.toString());
		if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
			log.error("[UPDATE WORKSPACE TITLE] Parameter Error Message : workspaceId=[{}]", workspaceId);
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[UPDATE WORKSPACE TITLE] Parameter Error Message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceTitleUpdateResponse workspaceTitleUpdateResponse = workspaceService.updateWorkspaceTitle(
			workspaceId, workspaceTitleUpdateRequest);
		return ResponseEntity.ok(new ApiResponse<>(workspaceTitleUpdateResponse));

	}

	@ApiOperation(value = "워크스페이스 로고 변경")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "로고 변경 유저 식별자", dataType = "string", paramType = "form", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608"),
		@ApiImplicitParam(name = "defaultLogo", value = "로고 이미지(생략 시 기본이미지)", dataType = "__file", paramType = "form", required = false),
		@ApiImplicitParam(name = "greyLogo", value = "로고 그레이 이미지", dataType = "__file", paramType = "form", required = false),
		@ApiImplicitParam(name = "whiteLogo", value = "로고 화이트 이미지", dataType = "__file", paramType = "form", required = false),
	})
	@PostMapping("/{workspaceId}/logo")
	public ResponseEntity<ApiResponse<WorkspaceLogoUpdateResponse>> updateWorkspaceLogo(
		@PathVariable("workspaceId") String workspaceId,
		@ModelAttribute @Valid WorkspaceLogoUpdateRequest workspaceLogoUpdateRequest, BindingResult bindingResult
	) {
		log.info(
			"[UPDATE_WORKSPACE_LOGO] WORKSPACE : {}, REQ : {}", workspaceId, workspaceLogoUpdateRequest.toString());
		if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
			log.error("[UPDATE WORKSPACE LOGO] Parameter Error Message : workspaceId=[{}]", workspaceId);
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[UPDATE WORKSPACE LOGO] Parameter Error Message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceLogoUpdateResponse workspaceLogoUpdateResponse = workspaceService.updateWorkspaceLogo(
			workspaceId, workspaceLogoUpdateRequest);
		return ResponseEntity.ok(new ApiResponse<>(workspaceLogoUpdateResponse));

	}

	@ApiOperation(value = "워크스페이스 파비콘 변경")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "파비콘 변경 유저 식별자", dataType = "string", paramType = "form", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608"),
		@ApiImplicitParam(name = "favicon", value = "파비콘 이미지(생략 시 기본이미지)", dataType = "__file", paramType = "form", required = false)
	})
	@PostMapping("/{workspaceId}/favicon")
	public ResponseEntity<ApiResponse<WorkspaceFaviconUpdateResponse>> updateWorkspaceFavicon(
		@PathVariable("workspaceId") String workspaceId,
		@ModelAttribute WorkspaceFaviconUpdateRequest workspaceFaviconUpdateRequest
	) {
		log.info(
			"[UPDATE_WORKSPACE_FAVICON] WORKSPACE : {}, REQ : {}", workspaceId,
			workspaceFaviconUpdateRequest.toString()
		);
		if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(workspaceFaviconUpdateRequest.getUserId())) {
			log.error(
				"[UPDATE WORKSPACE FAVICON] Parameter Error Message : workspaceId=[{}], workspaceFaviconUpdateRequest.getUserId()=[{}] ",
				workspaceId, workspaceFaviconUpdateRequest.getUserId()
			);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceFaviconUpdateResponse workspaceFaviconUpdateResponse = workspaceService.updateWorkspaceFavicon(
			workspaceId, workspaceFaviconUpdateRequest);
		return ResponseEntity.ok(new ApiResponse<>(workspaceFaviconUpdateResponse));
	}

	@ApiOperation(value = "워크스페이스 커스텀 설정 조회")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "query", required = false),
	})
	@GetMapping("/setting")
	public ResponseEntity<ApiResponse<WorkspaceCustomSettingResponse>> getWorkspaceCustomSetting(
		@RequestParam(value = "workspaceId", required = false) String workspaceId
	) {
		log.info("[GET_WORKSPACE_SETTING] WORKSPACE : {}", workspaceId);
		WorkspaceCustomSettingResponse workspaceCustomSettingResponse = workspaceService.getWorkspaceCustomSetting(
			workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(workspaceCustomSettingResponse));
	}

	@ApiOperation(value = "워크스페이스 리모트 서비스 로고 변경")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "로고 변경 유저 식별자", dataType = "string", paramType = "form", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608"),
		@ApiImplicitParam(name = "remoteAndroidLoginLogo", value = "안드로이드 스플래시 로고 이미지", dataType = "__file", paramType = "form", required = false),
		@ApiImplicitParam(name = "remoteAndroidSplashLogo", value = "안드로이드 로그인 로고 이미지", dataType = "__file", paramType = "form", required = false),
		@ApiImplicitParam(name = "remoteHololens2CommonLogo", value = "홀로렌즈2 로고 이미지", dataType = "__file", paramType = "form", required = false),
	})
	@PostMapping("/{workspaceId}/logo/remote")
	public ResponseEntity<ApiResponse<WorkspaceRemoteLogoUpdateResponse>> updateRemoteLogo(
		@PathVariable("workspaceId") String workspaceId,
		@ModelAttribute @Valid WorkspaceRemoteLogoUpdateRequest remoteLogoUpdateRequest, BindingResult bindingResult
	) {
		log.info("[UPDATE_REMOTE_LOGO] WORKSPACE : {}, REQ : {}", workspaceId, remoteLogoUpdateRequest.toString());
		if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
			log.error("[UPDATE REMOTE LOGO] Parameter Error Message : workspaceId=[{}]", workspaceId);
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[UPDATE REMOTE LOGO] Parameter Error Message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceRemoteLogoUpdateResponse logoUpdateResponse = workspaceService.updateRemoteLogo(
			workspaceId, remoteLogoUpdateRequest);
		return ResponseEntity.ok(new ApiResponse<>(logoUpdateResponse));

	}
}
