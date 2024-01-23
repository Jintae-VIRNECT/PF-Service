package com.virnect.workspace.global.common.mapper.rest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.dto.response.WorkspaceNewMemberInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserLicenseInfoResponse;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-12
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Mapper(componentModel = "spring")
public interface RestMapStruct {
	@Mapping(target = "nickName", source = "nickname")
	WorkspaceUserInfoResponse userInfoRestResponseToWorkspaceUserInfoResponse(UserInfoRestResponse userInfoRestResponse);

	@Mapping(target = "nickName", source = "nickname")
	WorkspaceNewMemberInfoResponse userInfoRestResponseToWorkspaceNewMemberInfoResponse(UserInfoRestResponse userInfoRestResponse);

	@Mapping(target = "nickName", source = "nickname")
	WorkspaceUserLicenseInfoResponse userInfoRestResponseToWorkspaceUserLicenseInfoResponse(UserInfoRestResponse userInfoRestResponse);
}
