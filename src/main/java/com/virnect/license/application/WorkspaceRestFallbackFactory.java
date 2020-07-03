package com.virnect.license.application;

import com.virnect.license.dto.rest.WorkspaceInfoListResponse;
import com.virnect.license.global.common.ApiResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-User
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.29
 */
@Slf4j
@Component
public class WorkspaceRestFallbackFactory implements FallbackFactory<WorkspaceRestService> {

    @Override
    public WorkspaceRestService create(Throwable cause) {
        log.error(cause.getMessage(), cause);
        return (userId, size) -> {
            log.info("[USER WORKSPACE LIST API FALLBACK] => USER_ID: {}", userId);
            WorkspaceInfoListResponse empty = new WorkspaceInfoListResponse();
            empty.setWorkspaceList(new ArrayList<>());
            return new ApiResponse<>(empty);
        };
    }
}
