package com.virnect.workspace.dao.group;

import com.virnect.workspace.domain.group.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public interface GroupRepository extends JpaRepository<Group,Long> {
    Optional<Group> findByName(String groupName);
}

