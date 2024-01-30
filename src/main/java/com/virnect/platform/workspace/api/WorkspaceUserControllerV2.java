package com.virnect.platform.workspace.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

import com.virnect.platform.workspace.application.workspaceuser.WorkspaceUserServiceV2;
import com.virnect.platform.workspace.domain.workspace.Role;
import com.virnect.platform.workspace.dto.response.WorkspaceUserInfoListResponse;
import com.virnect.platform.workspace.exception.WorkspaceException;
import com.virnect.platform.workspace.global.common.ApiResponse;
import com.virnect.platform.workspace.global.common.PageRequest;
import com.virnect.platform.workspace.global.error.ErrorCode;

/**
 * Project        : PF-Service
 * DATE           : 2024-01-30
 * AUTHOR         : jtkim (Jintae kim)
 * EMAIL          : jtkim@virnect.com
 * DESCRIPTION    :
 * ===========================================================
 * DATE            AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-01-30      jtkim          최초 생성
 */

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces/v2")
public class WorkspaceUserControllerV2 {

	private final WorkspaceUserServiceV2 workspaceUserService;

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
}
