package com.virnect.platform.workspace.dao.workspaceuser;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import com.virnect.platform.workspace.domain.workspace.QWorkspaceUser;
import com.virnect.platform.workspace.domain.workspace.Workspace;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-22
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkspaceUserRepositoryImpl implements WorkspaceUserRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;
	QWorkspaceUser qWorkspaceUser = QWorkspaceUser.workspaceUser;

	@Override
	public long deleteAllWorkspaceUserByWorkspace(Workspace workspace) {
		return jpaQueryFactory.delete(qWorkspaceUser).where(qWorkspaceUser.workspace.eq(workspace)).execute();
	}

	@Override
	public List<String> getWorkspaceUserIdList(String workspaceId) {
		return jpaQueryFactory.select(qWorkspaceUser.userId)
			.from(qWorkspaceUser)
			.where(qWorkspaceUser.workspace.uuid.eq(workspaceId)).fetch();
	}
}
