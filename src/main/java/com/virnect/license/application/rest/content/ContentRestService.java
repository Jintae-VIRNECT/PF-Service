package com.virnect.license.application.rest.content;

import java.time.LocalDateTime;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.virnect.license.dto.rest.content.ContentResourceUsageInfoResponse;
import com.virnect.license.global.common.ApiResponse;

@FeignClient(name = "content-server", fallbackFactory = ContentRestFallbackFactory.class)
public interface ContentRestService {

	@GetMapping("/contents/resources/report/{workspaceId}")
	ApiResponse<ContentResourceUsageInfoResponse> getContentResourceUsageInfoRequest(
		@PathVariable("workspaceId") String workspaceId,
		@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		@RequestParam(name = "startDate") LocalDateTime startDate,
		@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		@RequestParam(name = "endDate") LocalDateTime endDate
	);
}
