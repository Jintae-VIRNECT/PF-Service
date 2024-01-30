package com.virnect.platform.workspace.application.history;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.global.common.mapper.history.HistoryMapStruct;
import com.virnect.platform.workspace.dao.history.HistoryRepository;
import com.virnect.platform.workspace.domain.histroy.History;
import com.virnect.platform.workspace.dto.response.PageMetadataRestResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceHistoryListResponse;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {
	private final HistoryRepository historyRepository;
	private final HistoryMapStruct historyMapStruct;

	public WorkspaceHistoryListResponse getWorkspaceHistory(
		String workspaceId, String userId, Pageable pageable
	) {

		Page<History> historyPage = historyRepository.findAllByUserIdAndWorkspace_Uuid(userId, workspaceId, pageable);
		List<WorkspaceHistoryListResponse.WorkspaceHistory> workspaceHistoryList = historyPage.stream()
			.map(historyMapStruct::historyToWorkspaceHistory)
			.collect(Collectors.toList());

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(historyPage.getTotalElements());
		pageMetadataResponse.setTotalPage(historyPage.getTotalPages());
		pageMetadataResponse.setCurrentPage(pageable.getPageNumber());
		pageMetadataResponse.setCurrentSize(pageable.getPageSize());

		return new WorkspaceHistoryListResponse(workspaceHistoryList, pageMetadataResponse);
	}
}
