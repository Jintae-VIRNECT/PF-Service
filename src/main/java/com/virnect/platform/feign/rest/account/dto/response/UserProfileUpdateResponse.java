package com.virnect.platform.feign.rest.account.dto.response;

import org.thymeleaf.util.StringUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-10
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class UserProfileUpdateResponse {
	public static final UserProfileUpdateResponse EMPTY = new UserProfileUpdateResponse();
	@ApiModelProperty(value = "사용자 식별자", example = "498b1839dc29ed7bb2ee90ad6985c608")
	private String uuid;
	@ApiModelProperty(value = "프로필 이미지 url", example = "http://localhost:8081/users/upload/2020-04-07_ilzUZjnHMZqhoRpkqMUn.jpg")
	private String profile;

	public boolean isEmpty() {
		return StringUtils.isEmpty(uuid);
	}
}
