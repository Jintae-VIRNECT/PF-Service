package com.virnect.platform.workspace.dao.group;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.platform.workspace.domain.group.Group;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface GroupRepository extends JpaRepository<Group, Long> {
	Optional<Group> findByName(String groupName);
}

