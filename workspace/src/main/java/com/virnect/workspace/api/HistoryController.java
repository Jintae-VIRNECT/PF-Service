package com.virnect.workspace.api;

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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

import com.virnect.workspace.application.history.HistoryService;
import com.virnect.workspace.dto.response.WorkspaceHistoryListResponse;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.PageRequest;
import com.virnect.workspace.global.error.ErrorCode;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryController {
	private final HistoryService historyService;

	@ApiOperation(
		value = "워크스페이스 히스토리 조회",
		notes = "워크스페이스 내에서 사용자의 활동을 조회합니다."
	)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "workspaceId", value = "워크스페이스 uuid", dataType = "string", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", paramType = "path", required = true),
		@ApiImplicitParam(name = "userId", value = "사용자 uuid", dataType = "string", defaultValue = "498b1839dc29ed7bb2ee90ad6985c60", paramType = "query", required = true),
		@ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호", paramType = "query", defaultValue = "0"),
		@ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
		@ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "createdDate,desc")
	})
	@GetMapping("/{workspaceId}/history")
	public ResponseEntity<ApiResponse<WorkspaceHistoryListResponse>> getWorkspaceHistory(
		@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId,
		@ApiIgnore PageRequest pageRequest
	) {
		if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId)) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		WorkspaceHistoryListResponse responseMessage = historyService.getWorkspaceHistory(workspaceId, userId, pageRequest.of());
		return ResponseEntity.ok(new ApiResponse<>(responseMessage));
	}
}
