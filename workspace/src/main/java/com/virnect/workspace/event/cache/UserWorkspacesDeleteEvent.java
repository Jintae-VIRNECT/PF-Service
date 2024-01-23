package com.virnect.workspace.event.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class UserWorkspacesDeleteEvent {
	private final String userId;

	@Override
	public String toString() {
		return "UserWorkspacesDeleteEvent{" +
			"userId='" + userId + '\'' +
			'}';
	}
}
