package com.virnect.workspace.event.invite;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2021-07-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class InviteSessionDeleteEvent {
	private final String sessionCode;
}
