package com.virnect.workspace.application.user.dto.request;

import org.thymeleaf.util.StringUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class UserInfoAccessCheckRequest {
	@ApiModelProperty(value = "계정 이메일 아이디", example = "smic1")
	private final String email;
	@ApiModelProperty(value = "계정 비밀번호", position = 1, example = "smic1234")
	private final String password;

	@Override
	public String toString() {
		return "UserInfoAccessCheckRequest{" +
			"email='" + email + '\'' +
			", password='" + StringUtils.repeat("*", password.length()) + '\'' +
			'}';
	}
}
