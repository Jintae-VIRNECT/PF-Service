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
public class WorkspaceTitleUpdateResponse {
	@ApiModelProperty(value = "고객사명 변경 결과", example = "true", position = 0)
	private Boolean result;

	@ApiModelProperty(value = "고객사명", example = "", position = 1)
	private String title = "";
}
