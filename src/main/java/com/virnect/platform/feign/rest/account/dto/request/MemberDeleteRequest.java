package com.virnect.platform.feign.rest.account.dto.request;

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
public class MemberDeleteRequest {
	private String masterUUID;
	private String memberUserUUID;

	@Override
	public String toString() {
		return "MemberDeleteRequest{" +
			"masterUUID='" + masterUUID + '\'' +
			", memberUserUUID='" + memberUserUUID + '\'' +
			'}';
	}
}
