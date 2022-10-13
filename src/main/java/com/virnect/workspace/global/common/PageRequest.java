package com.virnect.workspace.global.common;

import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * @author jeonghyeon.chang (johnmark)
 * @email practice1356@gmail.com
 * @since 2020.03.02
 */

public final class PageRequest {
    private int page = 1;
    private int size = 20;
    private String sort;
    private static final int MAX_SIZE = 50;

    public void setPage(int page) {
        this.page = page <= 0 ? 1 : page;
    }

    public void setSize(int size) {
        this.size = Math.min(size, MAX_SIZE);
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public org.springframework.data.domain.PageRequest of() {
        // sort nullable
        String sortName = getSortName();
        String sortDirection = getSortDirection();

        if (!(sortDirection.equals("ASC") || sortDirection.equals("DESC"))) {
            sortDirection = "DESC";
        }

        if (sortName == null || sortName.isEmpty()) {
            sortName = "createdDate";
        }
        if (sortName.equalsIgnoreCase("role")) {
            sortName = "workspaceRole";
        }
        if (sortName.equalsIgnoreCase("joinDate")) {
            sortName = "workspaceUser.createdDate";
        }

        return org.springframework.data.domain.PageRequest.of(page - 1, size, Sort.Direction.valueOf(sortDirection), sortName);

    }

    public String getSortName(){
        String sortStr = Objects.isNull(this.sort) || this.sort.isEmpty() ? "updatedDate,DESC" : this.sort;
        String[] sortQuery = sortStr.split(",");
         return sortQuery[0];


    }
    public String getSortDirection(){
        String sortStr = Objects.isNull(this.sort) || this.sort.isEmpty() ? "updatedDate,DESC" : this.sort;
        String[] sortQuery = sortStr.split(",");
        return sortQuery[1].toUpperCase();
    }


}
