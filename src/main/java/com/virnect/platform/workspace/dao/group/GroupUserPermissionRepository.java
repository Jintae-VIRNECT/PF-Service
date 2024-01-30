package com.virnect.platform.workspace.dao.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.virnect.platform.workspace.domain.group.GroupUser;
import com.virnect.platform.workspace.domain.group.GroupUserPermission;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-30
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface GroupUserPermissionRepository extends JpaRepository<GroupUserPermission, Long> {
	@Transactional
	void deleteAllByGroupUser(GroupUser groupUser);
}
