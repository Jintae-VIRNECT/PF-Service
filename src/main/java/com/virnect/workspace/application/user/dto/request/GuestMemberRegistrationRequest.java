package com.virnect.workspace.application.user.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-12
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class GuestMemberRegistrationRequest {
	private String masterUserUUID;
	private String workspaceUUID;

	@Override
	public String toString() {
		return "GuestMemberRegistrationRequest{" +
			"masterUserUUID='" + masterUserUUID + '\'' +
			", workspaceUUID='" + workspaceUUID + '\'' +
			'}';
	}
}
