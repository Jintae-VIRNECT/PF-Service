package com.virnect.platform.feign.rest.account.dto.response;

import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.virnect.platform.workspace.dto.response.PageMetadataRestResponse;

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
	public static final UserInfoListRestResponse EMPTY = new UserInfoListRestResponse(
		Collections.emptyList(), new PageMetadataRestResponse());
	private final List<UserInfoRestResponse> userInfoList;
	private final PageMetadataRestResponse pageMeta;

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(userInfoList);
	}
}
