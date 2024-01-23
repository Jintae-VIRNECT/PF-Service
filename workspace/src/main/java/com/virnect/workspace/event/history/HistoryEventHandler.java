package com.virnect.workspace.event.history;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.dao.history.HistoryRepository;
import com.virnect.workspace.domain.histroy.History;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class HistoryEventHandler {
	private final HistoryRepository historyRepository;

	@EventListener(HistoryAddEvent.class)
	public void historyAddEventListener(HistoryAddEvent historyAddEvent) {
		History history = History.builder()
			.message(historyAddEvent.getMessage())
			.userId(historyAddEvent.getUserId())
			.workspace(historyAddEvent.getWorkspace())
			.build();
		log.info("[WORKSPACE HISTORY ADD EVENT] - [{}]", historyAddEvent);
		historyRepository.save(history);
	}
}
