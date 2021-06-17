package com.virnect.workspace.dao.group;

import com.virnect.workspace.domain.group.Group;
import com.virnect.workspace.domain.group.GroupUser;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import org.springframework.data.jpa.repository.JpaRepository;

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
