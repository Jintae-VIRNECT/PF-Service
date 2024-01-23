package com.virnect.workspace.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */

@Getter
@NoArgsConstructor
public class WorkspaceInfoResponse {
	private WorkspaceInfoDTO workspaceInfo;
	private List<WorkspaceUserInfoResponse> workspaceUserInfo;
	private long masterUserCount;
	private long manageUserCount;
	private long memberUserCount;
	private int remotePlanCount;
	private int makePlanCount;
	private int viewPlanCount;

	@Builder
	public WorkspaceInfoResponse(
		WorkspaceInfoDTO workspaceInfo,
		List<WorkspaceUserInfoResponse> workspaceUserInfo, long masterUserCount, long manageUserCount,
		long memberUserCount, int remotePlanCount, int makePlanCount, int viewPlanCount
	) {
		this.workspaceInfo = workspaceInfo;
		this.workspaceUserInfo = workspaceUserInfo;
		this.masterUserCount = masterUserCount;
		this.manageUserCount = manageUserCount;
		this.memberUserCount = memberUserCount;
		this.remotePlanCount = remotePlanCount;
		this.makePlanCount = makePlanCount;
		this.viewPlanCount = viewPlanCount;
	}
}
