package com.virnect.license.application;

import com.virnect.license.dto.rest.WorkspaceInfoListResponse;
import com.virnect.license.global.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
     * 소속된 워크스페이스 목록 조히 API
     *
     * @param userId - 사용자 식별 고유 번호
     * @return - 워크스페이스 정보 목록
     */
    @GetMapping("/workspaces")
    ApiResponse<WorkspaceInfoListResponse> getMyWorkspaceInfoList(@RequestParam("userId") String userId);
}
