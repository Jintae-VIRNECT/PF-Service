package com.virnect.workspace.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-10
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@RequiredArgsConstructor
@Getter
public class MemberProfileUpdateResponse {
	private final boolean result;
	private final String profile;
	private final String userId;
	private final LocalDateTime updatedDate;
}
