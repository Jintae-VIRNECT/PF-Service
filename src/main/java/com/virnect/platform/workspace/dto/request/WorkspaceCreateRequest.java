package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-18
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceCreateRequest {
	@NotBlank
	private String userId;

	@NotBlank
	@Length(min = 0, max = 30, message = "워크스페이스 이름은 최대 30자까지 가능합니다.")
	private String name;

	private MultipartFile profile;

	@NotBlank
	@Length(min = 0, max = 40, message = "워크스페이스 설명은 최대 40자까지 가능합니다.")
	private String description;

	@Override
	public String toString() {
		return "WorkspaceInfo{" +
			"userId='" + userId + '\'' +
			", description='" + description + '\'' +
			'}';
	}
}
