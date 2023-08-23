package com.virnect.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

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
public class WorkspaceTitleUpdateRequest {
	@ApiModelProperty(value = "고객사명 변경 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608")
	@NotBlank
	private String userId;

	@ApiModelProperty(value = "고객사명", required = true, position = 1)
	@NotBlank
	@Length(max = 20, message = "워크스페이스 고객사명은 최대 20자까지 가능합니다.")
	private String title;

	@Override
	public String toString() {
		return "WorkspaceTitleUpdateRequest{" +
			"userId='" + userId + '\'' +
			", title='" + title + '\'' +
			'}';
	}
}