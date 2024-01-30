package com.virnect.platform.workspace.dao.workspace;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.platform.workspace.domain.workspace.Workspace;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-08
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
public interface WorkspaceRepository extends JpaRepository<Workspace, Long>, WorkspaceRepositoryCustom {
	Optional<Workspace> findByUuid(String workspaceId);

	Optional<Workspace> findByUserId(String userUUID);

	boolean existsByUserId(String userId);

	Page<Workspace> findAll(Pageable pageable);

	long countByUserId(String userId);
}
