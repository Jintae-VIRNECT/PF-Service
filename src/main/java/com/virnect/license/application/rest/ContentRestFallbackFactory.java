package com.virnect.license.application.rest;

import com.virnect.license.dto.rest.ContentResourceUsageInfoResponse;
import com.virnect.license.global.common.ApiResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class ContentRestFallbackFactory implements FallbackFactory<ContentRestService> {

    @Override
    public ContentRestService create(Throwable cause) {
        log.info("[CONTENT_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
        log.error(cause.getMessage(), cause);
        return workspaceId -> {
            log.error("[CONTENT SERVER REST SERVICE FALLBACK ERROR][WORKSPACE_ID] -> [{}]", workspaceId);
            return new ApiResponse<>(new ContentResourceUsageInfoResponse(workspaceId, 0, 0, LocalDateTime.now()));
        };
    }
}
