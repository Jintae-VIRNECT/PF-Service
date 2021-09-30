package com.virnect.workspace.dao.workspace;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.virnect.workspace.domain.workspace.QWorkspaceUserPermission;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Optional;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class WorkspaceUserPermissionRepositoryImpl extends QuerydslRepositorySupport implements WorkspaceUserPermissionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private static final String ALL_WORKSAPCE_ROLE = ".*(?i)MASTER.*|.*(?i)MANAGER.*|.*(?i)MEMBER.*";

    public WorkspaceUserPermissionRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        super(WorkspaceUserPermission.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }


    @Override
    public long deleteAllWorkspaceUserPermissionByWorkspaceUser(List<WorkspaceUser> workspaceUserList) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        return jpaQueryFactory.delete(qWorkspaceUserPermission).where(qWorkspaceUserPermission.workspaceUser.in(workspaceUserList)).execute();
    }

    @Override
    public Page<WorkspaceUserPermission> getContainedUserIdList(List<String> userIdList, Pageable pageable, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId), inUserIdList(userIdList))
                .fetchAll();
        List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
    }


    private BooleanExpression inUserIdList(List<String> userIdList) {
        return QWorkspaceUserPermission.workspaceUserPermission.workspaceUser.userId.in(userIdList);
    }

    @Override
    public Page<WorkspaceUserPermission> getWorkspaceUserList(Pageable pageable, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId))
                .fetchAll();
        List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
    }

    @Override
    public List<WorkspaceUserPermission> findRecentWorkspaceUserList(int size, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId))
                .orderBy(qWorkspaceUserPermission.workspaceUser.createdDate.desc())
                .limit(size);
        return query.fetch();
    }

    @Override
    public Optional<WorkspaceUserPermission> findWorkspaceUser(String workspaceId, String userId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId).and(qWorkspaceUserPermission.workspaceUser.userId.eq(userId)));
        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    public List<String> getUserIdsByInUserListAndEqRole(List<String> userIdList, List<Role> roleList, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<String> query = jpaQueryFactory
                .select(qWorkspaceUserPermission.workspaceUser.userId)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId), inUserIdList(userIdList),
                        qWorkspaceUserPermission.workspaceRole.role.in(roleList))
                .fetchAll();
        return query.fetch();
    }

    @Override
    public Page<WorkspaceUserPermission> getWorkspaceUserListByInUserList(List<String> userIdList, Pageable pageable, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId), inUserIdList(userIdList))
                .fetchAll();
        List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
    }
}
