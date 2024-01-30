package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceUpdateRequest {
	@NotBlank
	private String workspaceId;
	@NotBlank
	private String userId;

	@NotBlank(message = "워크스페이스 이름은 반드시 입력되어야 합니다")
	@Length(max = 30, message = "워크스페이스 이름은 최대 30자까지 가능합니다.")
	private String name;

	private MultipartFile profile;

	@NotBlank(message = "워크스페이스 설명은 반드시 입력되어야합니다.")
	@Length(max = 40, message = "워크스페이스 설명은 최대 40자까지 가능합니다.")
	private String description;
}
