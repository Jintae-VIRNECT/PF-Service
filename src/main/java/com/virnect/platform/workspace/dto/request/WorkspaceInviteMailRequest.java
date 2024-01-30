package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceInviteMailRequest {
	@ApiModelProperty(value = "초대한 사용자 닉네임")
	@NotBlank(message = "초대한 마스터 또는 매니저 유저의 닉네임은 필수 값입니다.")
	private String requestUserNickName;//워스크페이스 마스터 닉네임

	@ApiModelProperty(value = "초대한 사용자 email")
	@NotBlank(message = "초대한 마스터 또는 매니저 유저의 UUID는 필수 값입니다.")
	private String requestUserEmail;//마스터 이메일 계정

	@ApiModelProperty(value = "초대받은 사용자 닉네임")
	@NotBlank(message = "초대한 마스터 또는 매니저 유저의 닉네임은 필수 값입니다.")
	private String responseUserNickName;//초대된 사용자 닉네임

	@ApiModelProperty(value = "초대받은 사용자 이름")
	@NotBlank(message = "초대한 마스터 또는 매니저 유저의 이름은 필수 값입니다.")
	private String responseUserName;//초대된 사용자 이름

	@ApiModelProperty(value = "초대한 사용자 email")
	@NotBlank(message = "초대한 마스터 또는 매니저 유저의 UUID는 필수 값입니다.")
	private String responseUserEmail;//초대된 사용자 이메일 계정

	@ApiModelProperty(value = "초대한 사용자 권한")
	@NotBlank(message = "초대 권한은 필수 값입니다.")
	private String role;

	@ApiModelProperty(value = "초대 대상 워크스페이스 이름")
	@NotBlank(message = "초대 대상 워크스페이스 이름은 필수 값입니다.")
	private String workspaceName;

	@ApiModelProperty(value = "초대 수락 링크")
	@NotBlank(message = "초대 수락링크는 필수 값입니다.")
	private String acceptUrl;

	@ApiModelProperty(value = "초대 거절 링크")
	@NotBlank(message = "초대 거절링크는 필수 값입니다.")
	private String rejectUrl;

}
