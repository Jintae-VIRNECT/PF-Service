package com.virnect.platform.workspace.api;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.workspace.application.workspace.WorkspaceServiceV2;
import com.virnect.platform.workspace.dto.response.WorkspaceInfoResponse;
import com.virnect.platform.workspace.exception.WorkspaceException;
import com.virnect.platform.workspace.global.common.ApiResponse;
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
@RestController
@Slf4j
@RequestMapping("/workspaces/v2")
@RequiredArgsConstructor
public class WorkspaceControllerV2 {
	private final WorkspaceServiceV2 workspaceService;

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
}
