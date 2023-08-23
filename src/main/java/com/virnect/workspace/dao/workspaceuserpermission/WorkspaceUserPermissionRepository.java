package com.virnect.workspace.dao.workspaceuserpermission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceUserPermissionRepository
	extends JpaRepository<WorkspaceUserPermission, Long>, WorkspaceUserPermissionRepositoryCustom {
	Optional<WorkspaceUserPermission> findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
		Workspace workspace, String userId
	);

	@Transactional
	void deleteAllByWorkspaceUser(WorkspaceUser workspaceUser);

	List<WorkspaceUserPermission> findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(Workspace workspace, Role role);

	List<WorkspaceUserPermission> findByWorkspaceUser_UserId(String userId);
}
