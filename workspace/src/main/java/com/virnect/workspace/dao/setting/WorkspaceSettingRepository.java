package com.virnect.workspace.dao.setting;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.workspace.domain.workspace.WorkspaceSetting;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceSettingRepository extends JpaRepository<WorkspaceSetting, Long> {
	WorkspaceSetting findByWorkspaceId(String workspaceId);
}
