package com.virnect.workspace.dao.workspaceuserpermission;

import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceUserPermissionRepository extends JpaRepository<WorkspaceUserPermission, Long>, WorkspaceUserPermissionRepositoryCustom {
    Optional<WorkspaceUserPermission> findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(Workspace workspace, String userId);
    Page<WorkspaceUserPermission> findByWorkspaceUser_UserId(String userId, Pageable pageable);
    List<WorkspaceUserPermission> findByWorkspaceUser_Workspace(Workspace workspace);
    @Transactional
    void deleteAllByWorkspaceUser(WorkspaceUser workspaceUser);
    List<WorkspaceUserPermission> findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(Workspace workspace, Role role);
    List<WorkspaceUserPermission> findByWorkspaceUser_UserId(String userId);

}
