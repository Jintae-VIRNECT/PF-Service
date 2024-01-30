package com.virnect.platform.feign.rest.account.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-11-18
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class InviteUserDetailInfoResponse {
	@ApiModelProperty(value = "사용자 식별자", position = 0, example = "498b1839dc29ed7bb2ee90ad6985c608")
	private String userUUID;
	@ApiModelProperty(value = "사용자 이메일", position = 1, example = "test@test.com")
	private String email;
	@ApiModelProperty(value = "사용자 이름", position = 2, example = "홍길동")
	private String name;
	@ApiModelProperty(value = "사용자 이름", position = 3, example = "길동")
	private String firstName;
	@ApiModelProperty(value = "사용자 성", position = 4, example = "홍")
	private String lastName;
	@ApiModelProperty(value = "사용자 닉네임", position = 5, example = "화려한 조명이 나를 감싸네")
	private String nickname;
}
