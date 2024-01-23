package com.virnect.license.application.rest.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.virnect.license.dto.rest.remote.FileStorageInfoResponse;
import com.virnect.license.global.common.ApiResponse;

@FeignClient(name = "remote-service-server", fallbackFactory = RemoteRestFallbackFactory.class)
public interface RemoteRestService {

	@GetMapping("/remote/file/storage/capacity/{workspaceId}")
	ApiResponse<FileStorageInfoResponse> getStorageSizeFromRemoteServiceByWorkspaceId(
		@PathVariable("workspaceId") String workspaceId
	);
}
