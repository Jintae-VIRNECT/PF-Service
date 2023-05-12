package com.virnect.workspace.dao.workspaceuser;

import java.util.List;

import com.virnect.workspace.domain.workspace.Workspace;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-22
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceUserRepositoryCustom {
	long deleteAllWorkspaceUserByWorkspace(Workspace workspace);

	List<String> getWorkspaceUserIdList(String workspaceId);
}
