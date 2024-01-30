package com.virnect.platform.global.common.mapper.rest;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceNewMemberInfoResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceUserLicenseInfoResponse;

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
