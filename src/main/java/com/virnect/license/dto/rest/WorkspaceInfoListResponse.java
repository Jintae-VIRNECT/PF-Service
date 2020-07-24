package com.virnect.license.dto.rest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Project: PF-User
 * DATE: 2020-02-17
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@Getter
@Setter
@NoArgsConstructor
public class WorkspaceInfoListResponse {
    private List<WorkspaceInfoResponse> workspaceList;
}
