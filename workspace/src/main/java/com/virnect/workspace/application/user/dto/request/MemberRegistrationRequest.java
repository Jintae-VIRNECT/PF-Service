package com.virnect.workspace.application.user.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@ApiModel
public class MemberRegistrationRequest {
	private String email;
	private String password;
	private String masterUUID;

	@Override
	public String toString() {
		return "RegisterMemberRequest{" +
			"email='" + email + '\'' +
			", password='" + password + '\'' +
			", masterUUID='" + masterUUID + '\'' +
			'}';
	}
}
