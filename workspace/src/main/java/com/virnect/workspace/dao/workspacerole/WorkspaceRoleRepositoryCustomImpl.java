package com.virnect.workspace.dao.workspacerole;

import java.util.Optional;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.virnect.workspace.domain.workspace.QWorkspaceUserPermission;
import com.virnect.workspace.domain.workspace.WorkspaceRole;

/**
 * Project: PF-Workspace
 * DATE: 2021-11-24
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class WorkspaceRoleRepositoryCustomImpl extends QuerydslRepositorySupport
	implements WorkspaceRoleRepositoryCustom {
	public WorkspaceRoleRepositoryCustomImpl() {
		super(WorkspaceRole.class);
	}

	@Override
	public Optional<WorkspaceRole> findWorkspaceRole(String workspaceId, String userId) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		return Optional.ofNullable(
			from(qWorkspaceUserPermission)
				.select(qWorkspaceUserPermission.workspaceRole)
				.where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId)
					.and(qWorkspaceUserPermission.workspaceUser.userId.eq(userId)))
				.fetchOne());
	}
}
