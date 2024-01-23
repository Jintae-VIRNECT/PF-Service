package com.virnect.workspace.dto.response;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
public class WorkspaceUserInfoListResponse {
	private final List<WorkspaceUserInfoResponse> memberInfoList;
	private final PageMetadataRestResponse pageMeta;

	public WorkspaceUserInfoListResponse(
		List<WorkspaceUserInfoResponse> memberInfoList,
		PageMetadataRestResponse pageMeta
	) {
		this.memberInfoList = memberInfoList;
		this.pageMeta = pageMeta;
	}

	public WorkspaceUserInfoListResponse(
		List<WorkspaceUserInfoResponse> memberInfoList
	) {
		this.memberInfoList = memberInfoList;
		this.pageMeta = new PageMetadataRestResponse();
	}

	public WorkspaceUserInfoListResponse() {
		this.memberInfoList = Collections.emptyList();
		this.pageMeta = new PageMetadataRestResponse();
	}

}
