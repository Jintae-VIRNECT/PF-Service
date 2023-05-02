package com.virnect.workspace.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

import com.virnect.workspace.domain.workspace.Role;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceNewMemberInfoResponse {
	private String uuid;
	private String nickName;
	private String name;
	private String email;
	private String profile;
	private Role role;
	private LocalDateTime joinDate;
}
