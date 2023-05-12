package com.virnect.workspace.application.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Project: PF-Workspace
 * DATE: 2021-12-01
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@FeignClient(name = "remote-service-server", fallbackFactory = RemoteRestFallbackFactory.class)
public interface RemoteRestService {
	@PostMapping("/remote/guest/event")
	void sendGuestUserDeletedEvent(
		@RequestParam("event") String event, @RequestParam("userId") String userId, @RequestParam("workspaceId") String workspaceId
	);

}
