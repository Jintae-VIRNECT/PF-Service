package com.virnect.workspace.dao.workspaceuserpermission;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;

import com.virnect.workspace.domain.workspace.QWorkspacePermission;
import com.virnect.workspace.domain.workspace.QWorkspaceRole;
import com.virnect.workspace.domain.workspace.QWorkspaceUser;
import com.virnect.workspace.domain.workspace.QWorkspaceUserPermission;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class WorkspaceUserPermissionRepositoryImpl extends QuerydslRepositorySupport
	implements WorkspaceUserPermissionRepositoryCustom {
	private static final String ALL_WORKSAPCE_ROLE = ".*(?i)MASTER.*|.*(?i)MANAGER.*|.*(?i)MEMBER.*";

	public WorkspaceUserPermissionRepositoryImpl() {
		super(WorkspaceUserPermission.class);
	}

	@Override
	public long deleteAllWorkspaceUserPermissionByWorkspaceUser(List<WorkspaceUser> workspaceUserList) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		return delete(qWorkspaceUserPermission).where(qWorkspaceUserPermission.workspaceUser.in(workspaceUserList))
			.execute();
	}

	@Override
	public List<WorkspaceUserPermission> findRecentWorkspaceUserList(int size, String workspaceId) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		JPQLQuery<WorkspaceUserPermission> query = from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission)
			.where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId))
			.orderBy(qWorkspaceUserPermission.workspaceUser.createdDate.desc())
			.limit(size);
		return query.fetch();
	}

	@Override
	public Optional<WorkspaceUserPermission> findWorkspaceUserPermission(String workspaceId, String userId) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		JPQLQuery<WorkspaceUserPermission> query = from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission)
			.where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId)
				.and(qWorkspaceUserPermission.workspaceUser.userId.eq(userId)));
		return Optional.ofNullable(query.fetchOne());
	}

	@Override
	public List<String> getUserIdsByInUserListAndEqRole(
		List<String> userIdList, List<Role> roleList, String workspaceId
	) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		JPQLQuery<String> query = from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission.workspaceUser.userId)
			.where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId), inUserIdList(userIdList),
				qWorkspaceUserPermission.workspaceRole.role.in(roleList)
			)
			.fetchAll();
		return query.fetch();
	}

	@Override
	public Page<WorkspaceUserPermission> getWorkspaceUserPageByInUserList(
		List<String> userIdList, Pageable pageable, String workspaceId
	) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		JPQLQuery<WorkspaceUserPermission> query = from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission)
			.join(qWorkspaceUserPermission.workspaceRole, QWorkspaceRole.workspaceRole).fetchJoin()
			.join(qWorkspaceUserPermission.workspaceUser, QWorkspaceUser.workspaceUser).fetchJoin()
			.where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId), inUserIdList(userIdList))
			.fetchAll();
		List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query)
			.fetch();
		return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
	}

	@Override
	public List<WorkspaceUserPermission> getWorkspaceUserListByInUserList(
		List<String> userIdList, String workspaceId
	) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		return from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission)
			.join(QWorkspaceUserPermission.workspaceUserPermission.workspaceRole, QWorkspaceRole.workspaceRole)
			.fetchJoin()
			.join(qWorkspaceUserPermission.workspaceUser, QWorkspaceUser.workspaceUser)
			.fetchJoin()
			.where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId), inUserIdList(userIdList))
			.fetch();
	}

	@Override
	public Page<WorkspaceUserPermission> findByWorkspaceUser_UserId(String userId, Pageable pageable) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
		JPQLQuery<WorkspaceUserPermission> query = from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission)
			.join(QWorkspaceUserPermission.workspaceUserPermission.workspaceUser, QWorkspaceUser.workspaceUser)
			.fetchJoin()
			.join(
				QWorkspaceUserPermission.workspaceUserPermission.workspacePermission,
				QWorkspacePermission.workspacePermission
			)
			.fetchJoin()
			.join(QWorkspaceUserPermission.workspaceUserPermission.workspaceRole, QWorkspaceRole.workspaceRole)
			.fetchJoin()
			.where(qWorkspaceUserPermission.workspaceUser.userId.eq(userId))
			.fetchAll();

		List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query)
			.fetch();

		return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
	}

	@Override
	public List<WorkspaceUserPermission> findByWorkspaceUser_Workspace(
		Workspace workspace
	) {
		QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;

		return from(qWorkspaceUserPermission)
			.select(qWorkspaceUserPermission)
			.where(qWorkspaceUserPermission.workspaceUser.workspace.eq(workspace))
			.join(qWorkspaceUserPermission.workspaceUser, QWorkspaceUser.workspaceUser).fetchJoin()
			.join(qWorkspaceUserPermission.workspaceRole, QWorkspaceRole.workspaceRole).fetchJoin()
			.orderBy(qWorkspaceUserPermission.workspaceUser.createdDate.desc())
			.fetch();
	}

	private BooleanExpression inUserIdList(List<String> userIdList) {
		return QWorkspaceUserPermission.workspaceUserPermission.workspaceUser.userId.in(userIdList);
	}

}
