package com.virnect.workspace.dao.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.workspace.domain.histroy.History;

/**
 * Project: PF-Workspace
 * DATE: 2020-05-12
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface HistoryRepository extends JpaRepository<History, Long>, HistoryCustomRepository {
	Page<History> findAllByUserIdAndWorkspace_Uuid(String userId, String workspaceId, Pageable pageable);
}
