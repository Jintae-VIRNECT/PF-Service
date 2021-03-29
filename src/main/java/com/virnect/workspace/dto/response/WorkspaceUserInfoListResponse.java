package com.virnect.workspace.dto.response;

import com.virnect.workspace.dto.rest.PageMetadataRestResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class WorkspaceUserInfoListResponse {
    private final List<WorkspaceUserInfoResponse> memberInfoList;
    private final PageMetadataRestResponse pageMeta;
}
