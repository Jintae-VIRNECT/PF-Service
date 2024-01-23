package com.virnect.workspace.dao.workspacerole;

import java.util.Optional;

import com.virnect.workspace.domain.workspace.WorkspaceRole;

/**
 * Project: PF-Workspace
 * DATE: 2021-11-24
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceRoleRepositoryCustom {
	Optional<WorkspaceRole> findWorkspaceRole(String workspaceId, String userId);
}
