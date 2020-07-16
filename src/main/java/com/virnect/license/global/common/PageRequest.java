package com.virnect.license.global.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * @author jeonghyeon.chang (johnmark)
 * @email practice1356@gmail.com
 * @since 2020.03.02
 */
@Getter
@Setter
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

    public String getSort() {
        return Objects.isNull(this.sort) || this.sort.isEmpty() ? "updatedDate,DESC" : sort;
    }

    public org.springframework.data.domain.PageRequest of() {
        // sort nullable
        String sortStr = Objects.isNull(this.sort) || this.sort.isEmpty() ? "updatedDate,DESC" : this.sort;
        String[] sortQuery = sortStr.split(",");
        String properties = sortQuery[0];
        String sort = sortQuery[1].toUpperCase();

        if (!(sort.equals("ASC") || sort.equals("DESC"))) {
            sort = "DESC";
        }

        return org.springframework.data.domain.PageRequest.of(page - 1, size, Sort.Direction.valueOf(sort), properties);
    }

    @Override
    public String toString() {
        return "PageRequest{" +
                "page=" + page +
                ", size=" + size +
                ", sort='" + sort + '\'' +
                '}';
    }
}
