package com.virnect.workspace.global.common.mapper.workspace;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspaceSetting;
import com.virnect.workspace.dto.response.WorkspaceCustomSettingResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoDTO;
import com.virnect.workspace.dto.response.WorkspaceInfoListResponse;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Mapper(componentModel = "spring")
public interface WorkspaceMapStruct {

	@Mapping(target = "masterUserId", source = "userId")
	WorkspaceInfoDTO workspaceToWorkspaceInfoDTO(Workspace workspace);

	WorkspaceInfoListResponse.WorkspaceInfo workspaceToWorkspaceInfo(Workspace workspace);

	@Mapping(target = "workspaceTitle", source = "title")
	WorkspaceCustomSettingResponse workspaceSettingToWorkspaceCustomSettingResponse(WorkspaceSetting workspaceSetting);
}
