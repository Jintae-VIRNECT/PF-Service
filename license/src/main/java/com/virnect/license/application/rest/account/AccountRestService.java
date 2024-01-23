package com.virnect.license.application.rest.account;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.virnect.license.dto.rest.user.UserInfoRestResponse;
import com.virnect.license.global.common.ApiResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-17
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */

@FeignClient(name = "account-server", fallbackFactory = AccountRestFallbackFactory.class)
public interface AccountRestService {
	/**
	 * 유저 정보 조회
	 *
	 * @param userId - 유저 고유 아이디
	 * @return - 유저 정보
	 */
	@GetMapping("/users/{userId}")
	ApiResponse<UserInfoRestResponse> getUserInfoByUserId(@PathVariable("userId") String userId);

	/**
	 * 유저 정보 조회
	 *
	 * @param userId - 유저 고유 아이디
	 * @return - 유저 정보
	 */
	@GetMapping("/users/billing/{userId}")
	ApiResponse<UserInfoRestResponse> getUserInfoByUserPrimaryId(@PathVariable("userId") long userId);
}

