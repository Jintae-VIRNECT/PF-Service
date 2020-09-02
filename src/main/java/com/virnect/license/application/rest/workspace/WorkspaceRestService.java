package com.virnect.license.application.rest.workspace;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.virnect.license.dto.rest.WorkspaceInfoListResponse;
import com.virnect.license.dto.rest.WorkspaceInfoResponse;
import com.virnect.license.global.common.ApiResponse;

/**
 * Project: user
 * DATE: 2020-01-17
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@FeignClient(name = "workspace-server", fallbackFactory = WorkspaceRestFallbackFactory.class)
public interface WorkspaceRestService {
	/**
	 * 소속된 워크스페이스 목록 조회 API
	 *
	 * @param userId - 사용자 식별 고유 번호
	 * @return - 워크스페이스 정보 목록
	 */
	@GetMapping("/workspaces")
	ApiResponse<WorkspaceInfoListResponse> getMyWorkspaceInfoList(
		@RequestParam("userId") String userId, @RequestParam("size") int size
	);

	/**
	 * 워크스페이스 정보 조회 API
	 * @param workspaceId - 워크스페이스 식별자
	 * @return - 워크스페이스 정보
	 */
	@GetMapping("/workspaces/{workspaceId}/info")
	ApiResponse<WorkspaceInfoResponse> getWorkspaceInfo(@PathVariable("workspaceId") String workspaceId);
}
