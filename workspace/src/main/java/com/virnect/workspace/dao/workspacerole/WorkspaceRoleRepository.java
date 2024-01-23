package com.virnect.workspace.dao.workspacerole;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.WorkspaceRole;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceRoleRepository extends JpaRepository<WorkspaceRole, Long>, WorkspaceRoleRepositoryCustom {
	Optional<WorkspaceRole> findByRole(Role roleName);
}
