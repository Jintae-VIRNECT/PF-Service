package com.virnect.workspace.domain.workspace;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-09
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public enum Role {
	MASTER,
	MANAGER,
	MEMBER,
	GUEST;

	public static List<Role> getMatchedList(String filter) {

		List<String> filterList = Arrays.asList(
			filter.toUpperCase().split(",").length == 0 ? new String[] {filter.toUpperCase()} :
				filter.toUpperCase().split(","));

		return Arrays.stream(Role.values()).
			filter(role -> filterList.contains(role.name()))
			.distinct().collect(Collectors.toList());
	}
}
