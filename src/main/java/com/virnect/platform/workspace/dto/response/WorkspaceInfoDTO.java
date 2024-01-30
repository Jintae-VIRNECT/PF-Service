package com.virnect.platform.workspace.dto.response;

import java.time.LocalDateTime;

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
public class WorkspaceInfoDTO {
	private long id;
	private String uuid;
	private String masterUserId;
	private String pinNumber;
	private String name;
	private String description;
	private String profile;
	private LocalDateTime updatedDate;
	private LocalDateTime createdDate;
}
