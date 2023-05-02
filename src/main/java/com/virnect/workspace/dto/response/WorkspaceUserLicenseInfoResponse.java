package com.virnect.workspace.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-06-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceUserLicenseInfoResponse {
	@ApiModelProperty(value = "유저 식별자", position = 0, example = "")
	private String uuid = "";
	@ApiModelProperty(value = "유저 프로필 이미지", position = 1, example = "")
	private String profile = "";
	@ApiModelProperty(value = "유저 닉네임", position = 2, example = "식물관리인")
	private String nickName = "";
	@ApiModelProperty(value = "현재 할당 가능한 라이선스 수량", position = 3, example = "REMOTE")
	private String productName = "";
	@ApiModelProperty(value = "현재 할당 된 라이선스 수량", position = 4, example = "BASIC")
	private String licenseType = "";

}
