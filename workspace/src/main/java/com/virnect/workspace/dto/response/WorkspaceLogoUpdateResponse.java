package com.virnect.workspace.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-15
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Setter
@Getter
public class WorkspaceLogoUpdateResponse {
	@ApiModelProperty(value = "로고 변경 결과", example = "true", position = 0)
	private Boolean result;

	@ApiModelProperty(value = "기본 로고 이미지 URL", example = "", position = 1)
	private String defaultLogo = "";

	@ApiModelProperty(value = "그레이 로고 이미지 URL", example = "", position = 2)
	private String greyLogo = "";

	@ApiModelProperty(value = "화이트 로고 이미지 URL", example = "", position = 3)
	private String whiteLogo = "";

}
