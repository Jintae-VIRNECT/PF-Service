package com.virnect.workspace.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@RequiredArgsConstructor
@Getter
public class WorkspaceInfoResponse {
	private final WorkspaceInfoDTO workspaceInfo;
	private final List<WorkspaceUserInfoResponse> workspaceUserInfo;
	private final long masterUserCount;
	private final long manageUserCount;
	private final long memberUserCount;
	private final int remotePlanCount;
	private final int makePlanCount;
	private final int viewPlanCount;
}
