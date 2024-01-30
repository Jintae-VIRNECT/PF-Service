package com.virnect.platform.workspace.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class PageMetadataRestResponse {
	@ApiModelProperty(value = "현재 조회한 페이지 번호", notes = "현재 페이지 번호", example = "0")
	private int currentPage;
	@ApiModelProperty(value = "현재 페이지별 데이터 수", notes = "현재 설정된 페이지별 나타날 데이터의 수", example = "2")
	private int currentSize;
	@ApiModelProperty(value = "페이지별 데이터 수에 따른 전체 페이지 수", notes = "설정된 페이지당 데이터 표출 수에 의한 전체 페이지 수", example = "10")
	private int totalPage;
	@ApiModelProperty(value = "조회 된 전체 데이터의 수", notes = "조회 된 전체 데이터의 수", example = "20")
	private long totalElements;
}
