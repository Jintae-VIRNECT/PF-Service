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
public class GuestMemberDeleteRequest {
	private String masterUUID;
	private String guestUserUUID;

	@Override
	public String toString() {
		return "GuestMemberDeleteRequest{" +
			"masterUUID='" + masterUUID + '\'' +
			", guestUserUUID='" + guestUserUUID + '\'' +
			'}';
	}
}
