package com.virnect.license.application.rest.content;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.content.ContentResourceUsageInfoResponse;
import com.virnect.license.global.common.ApiResponse;

@Slf4j
@Service
public class ContentRestFallbackFactory implements FallbackFactory<ContentRestService> {

	@Override
	public ContentRestService create(Throwable cause) {
		log.error("[CONTENT_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
		return (workspaceId, startDate, endDate) -> {
			log.error(
				"[CONTENT SERVER REST SERVICE FALLBACK ERROR][WORKSPACE_ID] -> [workspaceId: {}, startDate: {}, endDate: {}]",
				workspaceId, startDate, endDate
			);
			log.error(cause.getMessage(), cause);
			return new ApiResponse<>(new ContentResourceUsageInfoResponse(workspaceId, 0, 0, LocalDateTime.now()));
		};
	}
}
