package com.virnect.license.application.rest.account;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.user.UserInfoRestResponse;
import com.virnect.license.global.common.ApiResponse;

@Slf4j
@Service
public class AccountRestFallbackFactory implements FallbackFactory<AccountRestService> {
	@Override
	public AccountRestService create(Throwable cause) {
		return new AccountRestService() {
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
