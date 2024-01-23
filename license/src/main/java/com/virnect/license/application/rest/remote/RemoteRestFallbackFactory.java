package com.virnect.license.application.rest.remote;

import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.remote.FileStorageInfoResponse;
import com.virnect.license.global.common.ApiResponse;

@Slf4j
@Service
public class RemoteRestFallbackFactory implements FallbackFactory<RemoteRestService> {

	@Override
	public RemoteRestService create(Throwable cause) {
		log.error("[REMOTE_SERVICE_SERVER_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
		return (workspaceId) -> {
			log.error(
				"[REMOTE SERVICE SERVER REST SERVICE FALLBACK ERROR][WORKSPACE_ID] -> [workspaceId: {}]",
				workspaceId
			);
			log.error(cause.getMessage(), cause);
			return new ApiResponse<>(FileStorageInfoResponse.empty(workspaceId));
		};
	}
}
