package com.virnect.platform.workspace.dao.history;

import com.virnect.platform.workspace.domain.workspace.Workspace;

public interface HistoryCustomRepository {
	long deleteAllHistoryInfoByWorkspace(Workspace workspace);
}
