package com.virnect.platform.feign.rest.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.virnect.platform.global.common.ApiResponse;
import com.virnect.platform.license.dto.rest.remote.FileStorageInfoResponse;

@FeignClient(name = "remote-service-server", fallbackFactory = RemoteRestFallbackFactory.class)
public interface RemoteRestService {

	@GetMapping("/remote/file/storage/capacity/{workspaceId}")
	ApiResponse<FileStorageInfoResponse> getStorageSizeFromRemoteServiceByWorkspaceId(
		@PathVariable("workspaceId") String workspaceId
	);

	@PostMapping("/remote/guest/event")
	void sendGuestUserDeletedEvent(
		@RequestParam("event") String event, @RequestParam("userId") String userId, @RequestParam("workspaceId") String workspaceId
	);
}
