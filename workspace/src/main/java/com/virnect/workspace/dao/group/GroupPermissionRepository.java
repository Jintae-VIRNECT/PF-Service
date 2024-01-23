package com.virnect.workspace.dao.group;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.workspace.domain.group.GroupPermission;

/**
 * Project: PF-Workspace
 * DATE: 2020-03-02
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {
}
