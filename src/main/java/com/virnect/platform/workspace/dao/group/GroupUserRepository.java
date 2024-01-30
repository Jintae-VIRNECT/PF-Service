package com.virnect.platform.workspace.dao.group;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.platform.workspace.domain.group.Group;
import com.virnect.platform.workspace.domain.group.GroupUser;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUser;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-30
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {
	GroupUser findByWorkspaceUserAndGroup(WorkspaceUser workspaceUser, Group group);
}
