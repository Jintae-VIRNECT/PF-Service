package com.virnect.workspace.dao.workspacepermission;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.workspace.domain.workspace.WorkspacePermission;
import com.virnect.workspace.global.constant.Permission;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspacePermissionRepository extends JpaRepository<WorkspacePermission, Long> {
	Optional<WorkspacePermission> findByPermission(Permission permission);
}
