package com.virnect.workspace.application.remote;

import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * Project: PF-Workspace
 * DATE: 2021-12-01
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Component
public class RemoteRestFallbackFactory implements FallbackFactory<RemoteRestService> {
	@Override
	public RemoteRestService create(Throwable cause) {
		return new RemoteRestService() {
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
