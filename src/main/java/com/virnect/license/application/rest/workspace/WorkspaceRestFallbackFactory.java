package com.virnect.license.application.rest.workspace;

import java.util.ArrayList;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.user.WorkspaceInfoListResponse;
import com.virnect.license.dto.rest.user.WorkspaceInfoResponse;
import com.virnect.license.global.common.ApiResponse;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-User
 * @email practice1356@gmail.com
 * @description workspace rest service fallback factory class
 * @since 2020.04.29
 */
@Slf4j
@Service
public class WorkspaceRestFallbackFactory implements FallbackFactory<WorkspaceRestService> {
	@Override
	public WorkspaceRestService create(Throwable cause) {
		return new WorkspaceRestService() {
			@Override
			public ApiResponse<WorkspaceInfoListResponse> getMyWorkspaceInfoList(String userId, int size) {
				log.error("[USER WORKSPACE LIST API FALLBACK] => USER_ID: {}", userId);
				log.error(cause.getMessage(), cause);
				WorkspaceInfoListResponse empty = new WorkspaceInfoListResponse();
				empty.setWorkspaceList(new ArrayList<>());
				return new ApiResponse<>(empty);
			}

			@Override
			public ApiResponse<WorkspaceInfoResponse> getWorkspaceInfo(String workspaceId) {
				log.error("[USER WORKSPACE LIST API FALLBACK] => WORKSPACE_ID: {}", workspaceId);
				log.error(cause.getMessage(), cause);
				return new ApiResponse<>(new WorkspaceInfoResponse());
			}
		};
	}
}
