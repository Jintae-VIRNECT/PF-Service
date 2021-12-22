package com.virnect.workspace.application.user.dto.response;

import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.virnect.workspace.dto.response.PageMetadataRestResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class UserInfoListRestResponse {
	private final List<UserInfoRestResponse> userInfoList;
	private final PageMetadataRestResponse pageMeta;

	public static final UserInfoListRestResponse EMPTY = new UserInfoListRestResponse(
		Collections.emptyList(), new PageMetadataRestResponse());

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(userInfoList);
	}
}
