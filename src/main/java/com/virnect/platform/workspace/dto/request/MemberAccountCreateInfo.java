package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

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
public class MemberAccountCreateInfo {
	@ApiModelProperty(value = "계정 생성 아이디", required = true, example = "virnect", position = 0)
	@NotBlank
	@Pattern(regexp = "[a-zA-Z0-9]{4,20}")
	private String id;
	@ApiModelProperty(value = "계정 생성 패스워드", required = true, example = "virnect1234", position = 1)
	@NotBlank
	private String password;
	@ApiModelProperty(value = "워크스페이스 내의 권한", required = true, example = "MEMBER", position = 2)
	@NotBlank
	private String role;
	@ApiModelProperty(value = "리모트 제품 플랜 사용 여부", required = true, example = "true", position = 3)
	@NotNull
	private boolean planRemote;
	@ApiModelProperty(value = "메이크 제품 플랜 사용 여부", required = true, example = "false", position = 4)
	@NotNull
	private boolean planMake;
	@ApiModelProperty(value = "뷰 제품 플랜 사용 여부", required = true, example = "false", position = 5)
	@NotNull
	private boolean planView;

	@Override
	public String toString() {
		return "MemberAccountCreateInfo{" +
			"id='" + id + '\'' +
			", password='" + password + '\'' +
			", role='" + role + '\'' +
			", planRemote=" + planRemote +
			", planMake=" + planMake +
			", planView=" + planView +
			'}';
	}
}

