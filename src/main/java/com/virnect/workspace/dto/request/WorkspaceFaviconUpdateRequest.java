package com.virnect.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.workspace.global.validator.AllowedExtension;

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
public class WorkspaceFaviconUpdateRequest {
	@ApiModelProperty(value = "파비콘 변경 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608", position = 0)
	@NotBlank
	private String userId;

	@AllowedExtension(extensions = {AllowedExtension.Extension.PNG, AllowedExtension.Extension.JPG, AllowedExtension.Extension.ICO})
	@ApiModelProperty(value = "파비콘 이미지", required = true, position = 1)
	private MultipartFile favicon;

	@Override
	public String toString() {
		return "WorkspaceFaviconUpdateRequest{" +
			"userId='" + userId + '\'' +
			'}';
	}
}
