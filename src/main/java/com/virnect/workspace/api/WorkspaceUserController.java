package com.virnect.workspace.api;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

import com.virnect.workspace.application.workspaceuser.WorkspaceUserService;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.dto.request.MemberAccountCreateRequest;
import com.virnect.workspace.dto.request.MemberAccountDeleteRequest;
import com.virnect.workspace.dto.request.MemberGuestCreateRequest;
import com.virnect.workspace.dto.request.MemberGuestDeleteRequest;
import com.virnect.workspace.dto.request.MemberKickOutRequest;
import com.virnect.workspace.dto.request.MemberProfileUpdateRequest;
import com.virnect.workspace.dto.request.MemberUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.workspace.dto.request.WorkspaceMemberPasswordChangeRequest;
import com.virnect.workspace.dto.response.MemberProfileUpdateResponse;
import com.virnect.workspace.dto.response.MemberSeatDeleteResponse;
import com.virnect.workspace.dto.response.WorkspaceMemberInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceMemberPasswordChangeResponse;
import com.virnect.workspace.dto.response.WorkspaceNewMemberInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserLicenseListResponse;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.PageRequest;
import com.virnect.workspace.global.error.ErrorCode;

/**
 * Project: PF-Workspace
 * DATE: 2021-02-02
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkspaceUserController {
	private final WorkspaceUserService workspaceUserService;

	@ApiOperation(
		value = "워크스페이스 멤버 검색(워크스페이스 멤버 목록 조회)",
		notes = "워크스페이스 멤버 검색으로 멤버를 조회합니다. \n 필터링 우선순위 : 검색어 > 필터(권한 또는 라이선스) > 페이징"
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", defaultValue = "45ea004001c56a3380d48168b9db0492", required = true),
		@ApiImplicitParam(name = "search", value = "검색어(닉네임, 이메일)", dataType = "string", allowEmptyValue = true, defaultValue = ""),
		@ApiImplicitParam(name = "filter", value = "사용자 필터(MASTER, MANAGER, MEMBER) 또는 (REMOTE, MAKE, VIEW)", dataType = "string", allowEmptyValue = true, defaultValue = ""),
		@ApiImplicitParam(name = "role", value = "워크스페이스 역할 필터(MASTER, MANAGER, MEMBER, GUEST)", dataType = "string", allowEmptyValue = true, paramType = "query", allowMultiple = true),
		@ApiImplicitParam(name = "userType", value = "사용자 계정 타입 필터(USER, WORKSPACE_ONLY_USER, GUEST_USER)", dataType = "string", allowEmptyValue = true, defaultValue = ""),
		@ApiImplicitParam(name = "plan", value = "제품 라이선스 플랜 필터(REMOTE, MAKE, VIEW)", dataType = "string", allowEmptyValue = true, defaultValue = ""),
		@ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호", paramType = "query", defaultValue = "0"),
		@ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
		@ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터(role, joinDate, email, nickname)", paramType = "query", defaultValue = "role,desc"),
		@ApiImplicitParam(name = "paging", value = "정렬 여부", paramType = "query", defaultValue = "true"),
	})
	@GetMapping("/{workspaceId}/members")
	public ResponseEntity<ApiResponse<WorkspaceUserInfoListResponse>> getMembers(
		@PathVariable("workspaceId") String workspaceId,
		@RequestParam(value = "search", required = false) String search,
		@RequestParam(value = "filter", required = false) String filter,
		@RequestParam(value = "role", required = false) List<Role> role,
		@RequestParam(value = "userType", required = false) String userType,
		@RequestParam(value = "plan", required = false) String plan,
		@RequestParam(value = "paging", required = false, defaultValue = "true") boolean paging,
		@ApiIgnore PageRequest pageable
	) {
		if (!StringUtils.hasText(workspaceId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceUserInfoListResponse responseMessage = workspaceUserService.getMembers(
			workspaceId, search, filter, role, userType, plan, pageable, paging);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}

	@ApiOperation(
		value = "워크스페이스 신규 멤버 조회",
		notes = "워크스페이스 홈에서 워크스페이스의 신규 참여 멤버 정보를 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", defaultValue = "45ea004001c56a3380d48168b9db0492", required = true),
	})
	@GetMapping("/home/{workspaceId}/members")
	public ResponseEntity<ApiResponse<List<WorkspaceNewMemberInfoResponse>>> getWorkspaceNewUserInfo(
		@PathVariable("workspaceId") String workspaceId
	) {
		if (!StringUtils.hasText(workspaceId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		List<WorkspaceNewMemberInfoResponse> responseMessage = workspaceUserService.getWorkspaceNewUserInfo(
			workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}

	@ApiOperation(
		value = "워크스페이스 소속 유저 정보 편집",
		notes = "워크스페이스 소속 유저의 정보, 플랜, 역할 정보를 편집합니다. 편집의 권한은 워크스페이스 설정에 따라 달라질 수 있습니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	})
	@PostMapping("/{workspaceId}/members/info")
	public ResponseEntity<ApiResponse<Boolean>> reviseUserPermission(
		@PathVariable("workspaceId") String workspaceId, @RequestBody @Valid MemberUpdateRequest memberUpdateRequest,
		BindingResult bindingResult, @ApiIgnore Locale locale
	) {
		if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		ApiResponse<Boolean> apiResponse = workspaceUserService.reviseMemberInfo(workspaceId, memberUpdateRequest, locale);
		return ResponseEntity.ok(apiResponse);
	}

	@ApiOperation(
		value = "워크스페이스 멤버 정보 조회",
		notes = "워크스페이스 내에서 해당 멤버의 정보를 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "유저 uuid", dataType = "string", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", paramType = "query", required = true)
	})
	@GetMapping("/{workspaceId}/members/info")
	public ResponseEntity<ApiResponse<WorkspaceUserInfoResponse>> getMemberInfo(
		@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId
	) {
		if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceUserInfoResponse response = workspaceUserService.getMemberInfo(workspaceId, userId);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(
		value = "사용자 식별자 배열로 워크스페이스 멤버 정보 조회",
		notes = "워크스페이스 내에서 해당 멤버의 정보를 조회합니다."
	)
	@GetMapping("/{workspaceId}/members/infoList")
	public ResponseEntity<ApiResponse<WorkspaceUserInfoListResponse>> getMemberInfoList(
		@PathVariable("workspaceId") String workspaceId, @RequestParam(value = "userIds") String[] userIds
	) {
		if (!StringUtils.hasText(workspaceId) || userIds.length == 0) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceUserInfoListResponse response = workspaceUserService.getMemberInfoList(workspaceId, userIds);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(
		value = "워크스페이스 멤버 내보내기",
		notes = "마스터 또는 매니저 유저가 워크스페이스 내에서 해당 멤버 또는 매니저를 내보내기합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "요청 유저 uuid", dataType = "string", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", paramType = "form", required = true),
		@ApiImplicitParam(name = "kickedUserId", value = "내보내기 대상 유저 uuid", dataType = "string", defaultValue = "", paramType = "form", required = true)
	})
	@DeleteMapping("/{workspaceId}/members/info")
	public ResponseEntity<ApiResponse<Boolean>> kickOutMember(
		@PathVariable("workspaceId") String workspaceId,
		@ModelAttribute @Valid MemberKickOutRequest memberKickOutRequest, BindingResult bindingResult,
		@ApiIgnore Locale locale
	) {
		if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		ApiResponse<Boolean> apiResponse = workspaceUserService.kickOutMember(
			workspaceId, memberKickOutRequest, locale);
		return ResponseEntity.ok(apiResponse);
	}

	@ApiOperation(
		value = "워크스페이스 멤버 초대하기",
		notes = "워크스페이스 내에서 사용자를 초대합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	})
	@PostMapping("/{workspaceId}/invite")
	public ResponseEntity<ApiResponse<Boolean>> inviteWorkspace(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid WorkspaceInviteRequest workspaceInviteRequest,
		@ApiIgnore Locale locale,
		BindingResult bindingResult
	) {
		log.info("[WORKSPACE INVITE USER] Request - {}", workspaceInviteRequest.toString());
		if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		ApiResponse<Boolean> apiResponse = workspaceUserService.inviteWorkspace(
			workspaceId, workspaceInviteRequest, locale);
		return ResponseEntity.ok(apiResponse);
	}

	@ApiOperation(
		value = "워크스페이스 멤버 초대 수락",
		notes = "초대받은 사용자가 이메일 인증에서 초대를 수락합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "sessionCode", value = "초대 세션 코드", paramType = "path", required = true),
		@ApiImplicitParam(name = "lang", value = "언어", paramType = "query", required = false)
	})
	@GetMapping("/invite/{sessionCode}/accept")
	public RedirectView inviteWorkspaceAccept(
		@PathVariable("sessionCode") String sessionCode,
		@RequestParam(value = "lang", required = false) String lang
	) throws IOException {
		if (!StringUtils.hasText(sessionCode)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		return workspaceUserService.inviteWorkspaceAccept(sessionCode, lang);
	}

	@ApiOperation(
		value = "워크스페이스 멤버 초대 거절",
		notes = "초대받은 사용자가 이메일 인증에서 초대를 거절합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "sessionCode", value = "초대 세션 코드", paramType = "path", required = true),
		@ApiImplicitParam(name = "lang", value = "언어", paramType = "query", required = false)
	})
	@GetMapping("/invite/{sessionCode}/reject")
	public RedirectView inviteWorkspaceReject(
		@PathVariable("sessionCode") String sessionCode,
		@RequestParam(value = "lang", required = false) String lang
	) {
		if (!StringUtils.hasText(sessionCode)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		RedirectView redirectView = workspaceUserService.inviteWorkspaceReject(sessionCode, lang);
		return redirectView;
	}

	@ApiOperation(
		value = "워크스페이스 나가기",
		notes = "본인이 매니저 또는 멤버로 소속되어 있는 워크스페이스에서 나갑니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "유저 uuid", dataType = "string", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", paramType = "query", required = true),
	})
	@DeleteMapping("/{workspaceId}/exit")
	public ResponseEntity<ApiResponse<Boolean>> exitWorkspace(
		@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId, @ApiIgnore Locale locale
	) {
		if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		ApiResponse<Boolean> apiResponse = workspaceUserService.exitWorkspace(workspaceId, userId, locale);
		return ResponseEntity.ok(apiResponse);
	}

	@ApiOperation(
		value = "워크스페이스 멤버 조회",
		notes = "워크스페이스 내의 마스터, 매니저를 포함한 모든 멤버를 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
	})
	@GetMapping("/{workspaceId}/members/simple")
	public ResponseEntity<ApiResponse<WorkspaceUserInfoListResponse>> getSimpleWorkspaceUserList(
		@PathVariable("workspaceId") String workspaceId
	) {
		if (!StringUtils.hasText(workspaceId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceUserInfoListResponse response = workspaceUserService.getSimpleWorkspaceUserList(workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(
		value = "워크스페이스 플랜 사용자 조회",
		notes = "워크스페이스 내의 사용자를 플랜을 기준으로 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호", paramType = "query", defaultValue = "0"),
		@ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
		@ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "plan,desc")
	})
	@GetMapping("/{workspaceId}/members/license")
	public ResponseEntity<ApiResponse<WorkspaceUserLicenseListResponse>> getWorkspaceUserLicenseList(
		@PathVariable("workspaceId") String workspaceId, @ApiIgnore PageRequest pageRequest
	) {
		if (!StringUtils.hasText(workspaceId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceUserLicenseListResponse response = workspaceUserService.getLicenseWorkspaceUserList(workspaceId, pageRequest);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 전용 계정 생성")
	@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	@PostMapping("/{workspaceId}/members/account")
	public ResponseEntity<ApiResponse<WorkspaceMemberInfoListResponse>> createWorkspaceMemberAccount(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid MemberAccountCreateRequest memberAccountCreateRequest, BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[CREATE WORKSPACE MEMBER ACCOUNT] Error message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceMemberInfoListResponse response = workspaceUserService.createWorkspaceMemberAccount(
			workspaceId,
			memberAccountCreateRequest
		);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 전용 계정 삭제 및 내보내기")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	})
	@DeleteMapping("/{workspaceId}/members/account")
	public ResponseEntity<ApiResponse<Boolean>> removeWorkspaceMemberAccount(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid MemberAccountDeleteRequest memberAccountDeleteRequest, BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error(
						"[DELETE WORKSPACE MEMBER ACCOUNT] Parameter Error message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		Boolean response = workspaceUserService.deleteWorkspaceMemberAccount(workspaceId, memberAccountDeleteRequest);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 전용계정 비밀번호 재설정")
	@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	@PostMapping("/{workspaceId}/members/password")
	public ResponseEntity<ApiResponse<WorkspaceMemberPasswordChangeResponse>> memberPasswordChangeRequest(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid WorkspaceMemberPasswordChangeRequest passwordChangeRequest,
		BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors().forEach(
				objectError -> log.error("[WORKSPACE MEMBER PASSWORD CHANGE] Error message : [{}]", objectError)
			);
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceMemberPasswordChangeResponse response = workspaceUserService.memberPasswordChange(passwordChangeRequest, workspaceId);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 게스트 계정 생성")
	@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	@PostMapping("/{workspaceId}/members/guest")
	public ResponseEntity<ApiResponse<WorkspaceMemberInfoListResponse>> createWorkspaceMemberAccount(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid MemberGuestCreateRequest memberGuestCreateRequest, BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[CREATE WORKSPACE GUEST MEMBER] Error message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceMemberInfoListResponse response = workspaceUserService.createWorkspaceMemberGuest(
			workspaceId,
			memberGuestCreateRequest
		);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 게스트 계정 삭제 및 내보내기")
	@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true)
	@DeleteMapping("/{workspaceId}/members/guest")
	public ResponseEntity<ApiResponse<MemberSeatDeleteResponse>> deleteWorkspaceMemberAccount(
		@PathVariable("workspaceId") String workspaceId,
		@RequestBody @Valid MemberGuestDeleteRequest memberGuestDeleteRequest, BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[DELETE WORKSPACE GUEST MEMBER] Error message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		MemberSeatDeleteResponse response = workspaceUserService.deleteWorkspaceMemberSeat(
			workspaceId,
			memberGuestDeleteRequest
		);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

	@ApiOperation(value = "워크스페이스 전용계정/게스트계정 프로필 이미지 변경")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "profile", value = "워크스페이스 프로필", dataType = "__file", paramType = "form"),
	})
	@PostMapping("/{workspaceId}/members/profile")
	public ResponseEntity<ApiResponse<MemberProfileUpdateResponse>> updateWorkspaceOnlyUserOrGuestUserProfile(
		@PathVariable("workspaceId") String workspaceId,
		@ModelAttribute @Valid MemberProfileUpdateRequest memberProfileUpdateRequest, BindingResult bindingResult
	) {
		if (bindingResult.hasErrors()) {
			bindingResult.getAllErrors()
				.forEach(
					objectError -> log.error("[UPDATE WORKSPACE USER PROFILE] Error message : [{}]", objectError));
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		MemberProfileUpdateResponse response = workspaceUserService.updateWorkspaceOnlyUserOrGuestUserProfile(
			workspaceId, memberProfileUpdateRequest);
		return ResponseEntity.ok(new ApiResponse<>(response));
	}

}
