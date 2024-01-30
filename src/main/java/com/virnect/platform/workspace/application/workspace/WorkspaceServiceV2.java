package com.virnect.platform.workspace.application.workspace;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.virnect.platform.feign.rest.account.AccountRestService;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse;
import com.virnect.platform.global.common.mapper.workspace.WorkspaceMapStruct;
import com.virnect.platform.license.application.LicenseService;
import com.virnect.platform.license.dto.license.response.LicenseProductInfoResponse;
import com.virnect.platform.license.dto.license.response.WorkspaceLicensePlanInfoResponse;
import com.virnect.platform.workspace.dao.workspace.WorkspaceRepository;
import com.virnect.platform.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.platform.workspace.domain.workspace.Role;
import com.virnect.platform.workspace.domain.workspace.Workspace;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.platform.workspace.dto.response.WorkspaceInfoDTO;
import com.virnect.platform.workspace.dto.response.WorkspaceInfoResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.platform.workspace.exception.WorkspaceException;
import com.virnect.platform.workspace.global.constant.LicenseProduct;
import com.virnect.platform.workspace.global.error.ErrorCode;
import com.virnect.platform.workspace.global.util.CollectionJoinUtil;

/**
 * Project        : PF-Service
 * DATE           : 2024-01-30
 * AUTHOR         : jtkim (Jintae kim)
 * EMAIL          : jtkim@virnect.com
 * DESCRIPTION    :
 * ===========================================================
 * DATE            AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-01-30      jtkim          최초 생성
 */
@Service
@RequiredArgsConstructor
public class WorkspaceServiceV2 {
	private final AccountRestService accountRestService;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceMapStruct workspaceMapStruct;
	private final LicenseService licenseService;

	public WorkspaceInfoResponse getWorkspaceDetailInfo(String workspaceId) {
		//workspace 정보 set
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceInfoDTO workspaceInfo = workspaceMapStruct.workspaceToWorkspaceInfoDTO(workspace);
		workspaceInfo.setMasterUserId(workspace.getUserId());

		//user 정보 set
		List<WorkspaceUserPermission> workspaceUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_Workspace(
			workspace);
		List<String> workspaceUserIdList = workspaceUserPermissionList.stream()
			.map(workspaceUserPermission -> workspaceUserPermission.getWorkspaceUser().getUserId())
			.collect(Collectors.toList());

		List<UserInfoRestResponse> userInfoRestResponseList = accountRestService.getUserInfoList("", workspaceUserIdList)
			.getData()
			.getUserInfoList();

		List<WorkspaceUserInfoResponse> userInfoList = CollectionJoinUtil.collections(
				workspaceUserPermissionList, userInfoRestResponseList)
			.when((workspaceUserPermission, workspaceInfoResponse) -> workspaceInfoResponse.getUuid()
				.equals(workspaceUserPermission.getWorkspaceUser().getUserId()))
			.then(WorkspaceUserInfoResponse::createWorkspaceUserInfoResponse);

		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = licenseService.getWorkspaceLicensePlanInfo(workspaceId);

		return WorkspaceInfoResponse.builder()
			.workspaceInfo(workspaceInfo)
			.workspaceUserInfo(userInfoList)
			.memberUserCount(getRoleCount(workspaceUserPermissionList, Role.MEMBER))
			.manageUserCount(getRoleCount(workspaceUserPermissionList, Role.MANAGER))
			.masterUserCount(getRoleCount(workspaceUserPermissionList, Role.MASTER))
			.makePlanCount(getPlanCount(workspaceLicensePlanInfoResponse, LicenseProduct.MAKE))
			.remotePlanCount(getPlanCount(workspaceLicensePlanInfoResponse, LicenseProduct.REMOTE))
			.viewPlanCount(getPlanCount(workspaceLicensePlanInfoResponse, LicenseProduct.VIEW))
			.build();
	}

	private long getRoleCount(List<WorkspaceUserPermission> workspaceUserPermissionList, Role role) {
		return workspaceUserPermissionList.stream()
			.filter(workspaceUserPermission -> workspaceUserPermission.getWorkspaceRole().getRole() == role)
			.count();
	}

	private Integer getPlanCount(WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse, LicenseProduct licenseProduct) {

		return workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
			.stream()
			.filter(
				licenseProductInfoResponse -> licenseProduct.name().equals(licenseProductInfoResponse.getProductName()))
			.map(LicenseProductInfoResponse::getUseLicenseAmount)
			.findFirst()
			.orElse(0);
	}

}
