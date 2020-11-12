package com.virnect.workspace.dao;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.virnect.workspace.domain.QWorkspaceUserPermission;
import com.virnect.workspace.domain.WorkspaceRole;
import com.virnect.workspace.domain.WorkspaceUser;
import com.virnect.workspace.domain.WorkspaceUserPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class WorkspaceUserPermissionRepositoryImpl extends QuerydslRepositorySupport implements WorkspaceUserPermissionRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public WorkspaceUserPermissionRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        super(WorkspaceUserPermission.class);
        this.jpaQueryFactory=jpaQueryFactory;
    }

    @Override
    public WorkspaceRole findWorkspaceUserRole(String workspaceId, String userId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;

        return jpaQueryFactory.select(QWorkspaceUserPermission.workspaceUserPermission.workspaceRole)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId)
                        .and(qWorkspaceUserPermission.workspaceUser.userId.eq(userId))).fetchOne();
    }

    @Override
    public long deleteAllWorkspaceUserPermissionByWorkspaceUser(List<WorkspaceUser> workspaceUserList) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        return jpaQueryFactory.delete(qWorkspaceUserPermission).where(qWorkspaceUserPermission.workspaceUser.in(workspaceUserList)).execute();
    }

    @Override
    public Page<WorkspaceUserPermission> getRoleFilteredUserList(String roleFilter, Pageable pageable, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        /*
        QWorkspaceRole qWorkspaceRole= QWorkspaceRole.workspaceRole;
        List<WorkspaceRole> roleList = new ArrayList<>();
        if (roleFilter.contains("MASTER")) {
            roleList.add(WorkspaceRole.builder().id(1L).build());
        }
        if (roleFilter.contains("MANAGER")) {
            roleList.add(WorkspaceRole.builder().id(2L).build());
        }
        if (roleFilter.contains("MEMBER")) {
            roleList.add(WorkspaceRole.builder().id(3L).build());
        }*/

        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId).and(qWorkspaceUserPermission.workspaceRole.role.containsIgnoreCase(roleFilter)))
                .fetchAll();

        List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
    }

    @Override
    public Page<WorkspaceUserPermission> getContainedUserIdList(List<String> userIdList, Pageable pageable, String workspaceId) {
        QWorkspaceUserPermission qWorkspaceUserPermission = QWorkspaceUserPermission.workspaceUserPermission;
        JPQLQuery<WorkspaceUserPermission> query = jpaQueryFactory
                .select(qWorkspaceUserPermission)
                .from(qWorkspaceUserPermission)
                .where(qWorkspaceUserPermission.workspaceUser.workspace.uuid.eq(workspaceId).and(qWorkspaceUserPermission.workspaceUser.userId.in(userIdList)))
                .fetchAll();
        List<WorkspaceUserPermission> workspaceUserPermissionList = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(workspaceUserPermissionList, pageable, query.fetchCount());
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
}
