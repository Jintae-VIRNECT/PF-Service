package com.virnect.workspace.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-06-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@AllArgsConstructor
public class WorkspaceUserLicenseListResponse {
	public List<WorkspaceUserLicenseInfoResponse> workspaceUserLicenseInfoList;
	public PageMetadataRestResponse pageMeta;
}
