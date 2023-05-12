package com.virnect.workspace.global.common;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.virnect.workspace.dto.response.PageMetadataRestResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-11-11
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class CustomPageResponse<T> {
	private final List<T> afterPagingList;
	private final PageMetadataRestResponse pageMetadataResponse;
}
