package com.virnect.workspace.dao.history;

import com.virnect.workspace.domain.workspace.Workspace;

public interface HistoryCustomRepository {
	long deleteAllHistoryInfoByWorkspace(Workspace workspace);
}
