package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang.StringUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-05
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MemberGuestDeleteRequest {
	@ApiModelProperty(value = "요청 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608", position = 0)
	@NotBlank
	private String requestUserId;
	@ApiModelProperty(value = "요청 유저 패스워드", required = true, example = "test1234", position = 1)
	//@NotBlank
	private String requestUserPassword;
	@ApiModelProperty(value = "계정 삭제 대상 유저 식별자", required = true, example = "", position = 2)
	@NotBlank
	private String userId;

	@Override
	public String toString() {
		return "MemberGuestDeleteRequest{" +
			"requestUserId='" + requestUserId + '\'' +
			", requestUserPassword='" + StringUtils.repeat("*", requestUserPassword.length()) + '\'' +
			", userId='" + userId + '\'' +
			'}';
	}
}
