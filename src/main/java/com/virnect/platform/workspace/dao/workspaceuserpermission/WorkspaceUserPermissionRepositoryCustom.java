package com.virnect.platform.workspace.dao.workspaceuserpermission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.virnect.platform.workspace.domain.workspace.Role;
import com.virnect.platform.workspace.domain.workspace.Workspace;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUser;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUserPermission;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceUserPermissionRepositoryCustom {
	long deleteAllWorkspaceUserPermissionByWorkspaceUser(List<WorkspaceUser> workspaceUserList);

	List<WorkspaceUserPermission> findRecentWorkspaceUserList(int size, String workspaceId);

	Optional<WorkspaceUserPermission> findWorkspaceUserPermission(String workspaceId, String userId);

	List<String> getUserIdsByInUserListAndEqRole(List<String> userIdList, List<Role> roleList, String workspaceId);

	Page<WorkspaceUserPermission> getWorkspaceUserPageByInUserList(
		List<String> userIdList, Pageable newPageable, String workspaceId
	);

	List<WorkspaceUserPermission> getWorkspaceUserListByInUserList(List<String> userIdList, String workspaceId);

	Page<WorkspaceUserPermission> findByWorkspaceUser_UserId(String userId, Pageable pageable);

	List<WorkspaceUserPermission> findByWorkspaceUser_Workspace(Workspace workspace);
}
