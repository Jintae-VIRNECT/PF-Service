package com.virnect.workspace.dao.workspaceuser;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspaceUser;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-09
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long>, WorkspaceUserRepositoryCustom {
	List<WorkspaceUser> findByWorkspace_Uuid(String workspaceId);

	Page<WorkspaceUser> findByUserId(String userId, Pageable pageable);

	WorkspaceUser findByUserIdAndWorkspace(String userId, Workspace workspace);

	Optional<WorkspaceUser> findByUserIdAndWorkspace_Uuid(String userId, String workspaceId);

	List<WorkspaceUser> findTop4ByWorkspace_UuidOrderByCreatedDateDesc(String workspaceId);

	Page<WorkspaceUser> findByWorkspace_UuidAndUserIdIn(String workspaceId, List<String> userIdList, Pageable pageable);

	long countByUserId(String userId);

	long countByWorkspace_Uuid(String workspaceId);

	long deleteByWorkspace(Workspace workspace);

	List<WorkspaceUser> findByUserId(String userId);

}
