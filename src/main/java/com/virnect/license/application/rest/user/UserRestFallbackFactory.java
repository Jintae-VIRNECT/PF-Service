package com.virnect.license.application.rest.user;

import com.virnect.license.dto.rest.UserInfoRestResponse;
import com.virnect.license.global.common.ApiResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRestFallbackFactory implements FallbackFactory<UserRestService> {
    @Override
    public UserRestService create(Throwable cause) {
        return new UserRestService() {
            @Override
            public ApiResponse<UserInfoRestResponse> getUserInfoByUserId(String userId) {
                log.error("[USER_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
                log.error(cause.getMessage(), cause);
                ApiResponse apiResponse = new ApiResponse(null);
                apiResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                return apiResponse;
            }

            @Override
            public ApiResponse<UserInfoRestResponse> getUserInfoByUserPrimaryId(long userId) {
                log.error("[USER_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
                log.error(cause.getMessage(), cause);
                ApiResponse apiResponse = new ApiResponse(null);
                apiResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                return apiResponse;
            }
        };
    }
}
