package com.virnect.workspace.global.common.mapper.history;

import org.mapstruct.Mapper;

import com.virnect.workspace.domain.histroy.History;
import com.virnect.workspace.dto.response.WorkspaceHistoryListResponse;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Mapper(componentModel = "spring")
public interface HistoryMapStruct {
	WorkspaceHistoryListResponse.WorkspaceHistory historyToWorkspaceHistory(History history);
}
