package com.virnect.platform.workspace.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-05
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class MemberSeatDeleteResponse {
	private final boolean result;
	private final String deletedUserId;
	private final LocalDateTime deletedDate;
}
