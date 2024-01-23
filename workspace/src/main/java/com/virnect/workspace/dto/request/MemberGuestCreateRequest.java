package com.virnect.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-04
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MemberGuestCreateRequest {
	@ApiModelProperty(value = "계정 생성 요청 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608", position = 0)
	@NotBlank
	private String userId;

	@ApiModelProperty(value = "계정 생성 요청 유저 식별자", required = false, example = "0", position = 0)
	private Integer planRemoteAndView = 0;

	@ApiModelProperty(value = "계정 생성 요청 유저 식별자", required = false, example = "0", position = 0)
	private Integer planRemote = 0;

	@ApiModelProperty(value = "계정 생성 요청 유저 식별자", required = false, example = "0", position = 0)
	private Integer planView = 0;

	@Override
	public String toString() {
		return "MemberSeatCreateRequest{" +
			"userId='" + userId + '\'' +
			", planRemoteAndView=" + planRemoteAndView +
			", planRemote=" + planRemote +
			", planView=" + planView +
			'}';
	}
}
