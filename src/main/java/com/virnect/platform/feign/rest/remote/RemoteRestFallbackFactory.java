package com.virnect.platform.feign.rest.remote;

import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.global.common.ApiResponse;
import com.virnect.platform.license.dto.rest.remote.FileStorageInfoResponse;

@Slf4j
@Service
public class RemoteRestFallbackFactory implements FallbackFactory<RemoteRestService> {

	@Override
	public RemoteRestService create(Throwable cause) {
		log.error("[REMOTE_SERVICE_SERVER_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
		return new RemoteRestService() {
			@Override
			public ApiResponse<FileStorageInfoResponse> getStorageSizeFromRemoteServiceByWorkspaceId(String workspaceId) {
				log.error(
					"[REMOTE SERVICE SERVER REST SERVICE FALLBACK ERROR][WORKSPACE_ID] -> [workspaceId: {}]",
					workspaceId
				);
				log.error(cause.getMessage(), cause);
				return new ApiResponse<>(FileStorageInfoResponse.empty(workspaceId));
			}

			@Override
			public void sendGuestUserDeletedEvent(String event, String userId, String workspaceId) {
				log.error(
					"[REST - RM_SERVICE][FALLBACK] Send event fail. event : {}, userId : {}, workspaceId : {} failure cause : {}",
					event, userId, workspaceId, cause.getMessage()
				);
			}
		};
	}
}
