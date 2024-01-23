package com.virnect.workspace.dto.response;

import io.swagger.annotations.ApiModel;
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
@Getter
@Setter
@ApiModel
public class WorkspaceFaviconUpdateResponse {
	@ApiModelProperty(value = "로고 변경 결과", example = "true", position = 0)
	private Boolean result;

	@ApiModelProperty(value = "파비콘 이미지 URL", example = "", position = 1)
	private String favicon = "";
}
