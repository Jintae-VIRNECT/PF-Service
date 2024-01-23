package com.virnect.workspace.global.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.virnect.workspace.dto.response.PageMetadataRestResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-11-11
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class CustomPageHandler<T> {
	private int temp = 0;

	/**
	 * 커스텀 페이징 공통 모듈
	 *
	 * @param page          - 현재 조회할 페이지 번호
	 * @param pageSize         - 페이지 당 보여줄 요소 갯수
	 * @param beforePagingList - 페이징 처리하기 전의 리스트
	 * @return - 페이징 처리 후의 리스트
	 */
	public CustomPageResponse<T> paging(int page, int pageSize, List<T> beforePagingList) {
		int totalElements = beforePagingList.size();
		int size = pageSize;
		if (totalElements <= size) {
			size = totalElements;
		}

		int totalPage = totalElements / size;
		int resultPage = totalPage;
		int lastElements = totalElements % size;
		int currentPage = page + 1;
		if (lastElements > 0) {
			totalPage = totalPage + 1;
			resultPage = resultPage + 1;
		}

		List<List<T>> result = new ArrayList<>();

		while (totalPage > 0) {
			List<T> afterList = beforePagingList.stream().skip(temp).limit(size).collect(Collectors.toList());
			result.add(afterList);
			temp = temp + size;
			totalPage = totalPage - 1;
		}

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(totalElements);
		pageMetadataResponse.setTotalPage(resultPage);
		pageMetadataResponse.setCurrentPage(currentPage);
		pageMetadataResponse.setCurrentSize(pageSize);

		if (currentPage > result.size()) {
			return new CustomPageResponse(new ArrayList<>(), pageMetadataResponse);
		}
		return new CustomPageResponse(result.get(page), pageMetadataResponse);
	}
}
