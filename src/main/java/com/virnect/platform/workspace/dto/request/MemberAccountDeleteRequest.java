package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.apache.commons.lang.StringUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MemberAccountDeleteRequest {
	@ApiModelProperty(value = "요청 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608", position = 0)
	@NotBlank
	private String requestUserId;
	@ApiModelProperty(value = "요청 유저 패스워드", required = true, example = "test1234", position = 1)
	//@NotBlank
	private String requestUserPassword; //3Q 전용계정 타입 추가되면서 기획 상 사라짐. 다시 생김..
	@ApiModelProperty(value = "계정 삭제 대상 유저 식별자", required = true, example = "8NZQpwbIoniLQ", position = 2)
	@NotBlank
	private String userId;

	@ApiModelProperty(hidden = true)
	public boolean isUserSelfUpdateRequest() {
		return requestUserId.equals(userId);
	}

	@Override
	public String toString() {
		return "MemberAccountDeleteRequest{" +
			"requestUserId='" + requestUserId + '\'' +
			", requestUserPassword='" + StringUtils.repeat("*", requestUserPassword.length()) + '\'' +
			", userId='" + userId + '\'' +
			'}';
	}
}
