package com.virnect.workspace.dao.history;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.virnect.workspace.domain.histroy.QHistory;
import com.virnect.workspace.domain.workspace.Workspace;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HistoryCustomRepositoryImpl implements HistoryCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public long deleteAllHistoryInfoByWorkspace(Workspace workspace) {
        QHistory qHistory = QHistory.history;
        return jpaQueryFactory.delete(qHistory).where(qHistory.workspace.eq(workspace)).execute();
    }
}
