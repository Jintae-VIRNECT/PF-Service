package com.virnect.license.application.rest;

import com.virnect.license.dto.rest.ContentResourceUsageInfoResponse;
import com.virnect.license.global.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "content-server", fallbackFactory = ContentRestFallbackFactory.class)
//public interface ContentRestService {
//
//    @GetMapping("/contents/resources/report/{workspaceId}")
//    ApiResponse<ContentResourceUsageInfoResponse> getContentResourceUsageInfoRequest(@PathVariable("workspaceId") String workspaceId);
//}
