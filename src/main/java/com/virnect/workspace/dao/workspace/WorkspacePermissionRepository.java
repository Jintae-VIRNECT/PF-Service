package com.virnect.workspace.dao.workspace;

import com.virnect.workspace.domain.workspace.WorkspacePermission;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspacePermissionRepository extends JpaRepository<WorkspacePermission, Long> {
    WorkspacePermission findByPermission(String permission);
}
