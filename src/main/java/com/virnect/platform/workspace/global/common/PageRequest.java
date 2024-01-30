package com.virnect.platform.workspace.global.common;

import java.util.Objects;

import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import lombok.Setter;

/**
 * @author jeonghyeon.chang (johnmark)
 * @email practice1356@gmail.com
 * @since 2020.03.02
 */

@Setter
public final class PageRequest {
	private int page = 1;
	private int size;
	private String sort;

	public org.springframework.data.domain.PageRequest of() {
		// sort nullable
		String sortName = getSortName();
		String sortDirection = getSortDirection();

		if (!(sortDirection.equals("ASC") || sortDirection.equals("DESC"))) {
			sortDirection = "DESC";
		}

		if (StringUtils.isEmpty(sortName) || sortName.equalsIgnoreCase("email")
			|| sortName.equalsIgnoreCase("nickname")) {
			sortName = "createdDate";
		}
		if (sortName.equalsIgnoreCase("role")) {
			sortName = "workspaceRole";
		}
		if (sortName.equalsIgnoreCase("joinDate")) {
			sortName = "workspaceUser.createdDate";
		}

		if (size == 0) {
			setSize(Integer.MAX_VALUE);
		}
		if (page <= 0) {
			setPage(1);
		}

		return org.springframework.data.domain.PageRequest.of(
			page - 1, size, Sort.Direction.valueOf(sortDirection), sortName);

	}

	public String getSortName() {
		String sortStr = Objects.isNull(this.sort) || this.sort.isEmpty() ? "updatedDate,DESC" : this.sort;
		String[] sortQuery = sortStr.split(",");
		return sortQuery[0];
	}

	public String getSortDirection() {
		String sortStr = Objects.isNull(this.sort) || this.sort.isEmpty() ? "updatedDate,DESC" : this.sort;
		String[] sortQuery = sortStr.split(",");
		return sortQuery[1].toUpperCase();
	}

}
