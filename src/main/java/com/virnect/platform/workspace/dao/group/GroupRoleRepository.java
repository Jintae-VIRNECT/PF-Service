package com.virnect.platform.workspace.dao.group;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.platform.workspace.domain.group.GroupRole;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-30
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface GroupRoleRepository extends JpaRepository<GroupRole, Long> {
	GroupRole findByRole(String role);
}
