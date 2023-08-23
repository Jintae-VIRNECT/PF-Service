package com.virnect.workspace.event.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;

import com.virnect.workspace.application.user.dto.response.InviteUserDetailInfoResponse;
import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.workspace.global.common.RedirectProperty;
import com.virnect.workspace.global.constant.LicenseProduct;

/**
 * Project: PF-Workspace
 * DATE: 2021-07-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@RequiredArgsConstructor
@Component
public class MailContextHandler {
	private final RedirectProperty redirectProperty;

	public Context getWorkspaceInviteContext(
		String sessionCode, Locale locale, String workspaceName, WorkspaceInviteRequest.UserInfo userInfo,
		InviteUserDetailInfoResponse inviteUserInfo, UserInfoRestResponse masterUserInfo
	) {
		Context context = new Context();
		context.setVariable(
			"rejectUrl", redirectProperty.getWorkspaceServer() + "/workspaces/invite/" + sessionCode + "/reject?lang=" + locale.getLanguage());
		context.setVariable(
			"acceptUrl", redirectProperty.getWorkspaceServer() + "/workspaces/invite/" + sessionCode + "/accept?lang=" + locale.getLanguage());
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("role", userInfo.getRole());
		context.setVariable("plan", generatePlanString(userInfo.isPlanRemote(), userInfo.isPlanMake(), userInfo.isPlanView()));
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("responseUserEmail", userInfo.getEmail());
		context.setVariable("responseUserName", inviteUserInfo.getName());
		context.setVariable("responseUserNickName", inviteUserInfo.getNickname());
		return context;
	}

	private String generatePlanString(boolean remote, boolean make, boolean view) {
		return generatePlanList(remote, make, view).stream().map(Enum::toString).collect(Collectors.joining(","));
	}

	private List<LicenseProduct> generatePlanList(boolean remote, boolean make, boolean view) {
		List<LicenseProduct> productList = new ArrayList<>();
		if (remote) {
			productList.add(LicenseProduct.REMOTE);
		}
		if (make) {
			productList.add(LicenseProduct.MAKE);
		}
		if (view) {
			productList.add(LicenseProduct.VIEW);
		}
		return productList;
	}

	public Context getWorkspaceInviteNonUserContext(
		String sessionCode, Locale locale, String
		workspaceName, WorkspaceInviteRequest.UserInfo userInfo, UserInfoRestResponse masterUserInfo
	) {
		Context context = new Context();
		context.setVariable(
			"rejectUrl", redirectProperty.getWorkspaceServer() + "/workspaces/invite/" + sessionCode + "/reject?lang=" + locale.getLanguage());
		context.setVariable(
			"acceptUrl", redirectProperty.getWorkspaceServer() + "/workspaces/invite/" + sessionCode + "/accept?lang=" + locale.getLanguage());
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("role", userInfo.getRole());
		context.setVariable("plan", generatePlanString(userInfo.isPlanRemote(), userInfo.isPlanMake(), userInfo.isPlanView()));
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("responseUserEmail", userInfo.getEmail());
		return context;
	}

	public Context getWorkspaceInviteAcceptContext(
		String workspaceName, UserInfoRestResponse masterUserInfo, InviteUserDetailInfoResponse invitedUserinfo,
		String role, boolean isRemote, boolean isMake, boolean isView
	) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("acceptUserNickName", invitedUserinfo.getNickname());
		context.setVariable("acceptUserEmail", invitedUserinfo.getEmail());
		context.setVariable("role", role);
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("plan", generatePlanString(isRemote, isMake, isView));
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;

	}

	public Context getWorkspaceOverJoinContext(
		String workspaceName, UserInfoRestResponse masterUserInfo, InviteUserDetailInfoResponse invitedUserInfo,
		boolean isRemote, boolean isMake, boolean isView
	) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("userNickName", invitedUserInfo.getNickname());
		context.setVariable("userEmail", invitedUserInfo.getEmail());
		context.setVariable("plan", generatePlanString(isRemote, isMake, isView));
		//context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
		//context.setVariable("planMakeType", userInvite.getPlanMakeType());
		//context.setVariable("planViewType", userInvite.getPlanViewType());
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("workstationMembersUrl", redirectProperty.getMembersWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;
	}

	public Context getWorkspaceOverMaxUserContext(
		String workspaceName, UserInfoRestResponse masterUserInfo, InviteUserDetailInfoResponse inviteUserInfo, boolean isRemote, boolean isMake,
		boolean isView
	) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("userNickName", inviteUserInfo.getNickname());
		context.setVariable("userEmail", inviteUserInfo.getEmail());
		context.setVariable("plan", generatePlanString(isRemote, isMake, isView));
		//context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
		//context.setVariable("planMakeType", userInvite.getPlanMakeType());
		//context.setVariable("planViewType", userInvite.getPlanViewType());
		context.setVariable("contactUrl", redirectProperty.getContactWeb());
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;
	}

	public Context getWorkspaceOverPlanContext(
		String workspaceName, UserInfoRestResponse masterUserInfo, InviteUserDetailInfoResponse inviteUserInfo, List<String> successPlan,
		List<String> failPlan
	) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("userNickName", inviteUserInfo.getNickname());
		context.setVariable("userEmail", inviteUserInfo.getEmail());
		context.setVariable("successPlan", org.apache.commons.lang.StringUtils.join(successPlan, ","));
		context.setVariable("failPlan", org.apache.commons.lang.StringUtils.join(failPlan, ","));
		//context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
		//context.setVariable("planMakeType", userInvite.getPlanMakeType());
		//context.setVariable("planViewType", userInvite.getPlanViewType());
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("workstationMembersUrl", redirectProperty.getMembersWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;
	}

	public Context getWorkspaceInviteRejectContext(String workspaceName, InviteUserDetailInfoResponse inviteUserInfo) {
		Context context = new Context();
		context.setVariable("rejectUserNickname", inviteUserInfo.getNickname());
		context.setVariable("rejectUserEmail", inviteUserInfo.getEmail());
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("accountUrl", redirectProperty.getAccountWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;
	}

	public Context getWorkspaceUserPlanUpdateContext(
		String workspaceName, UserInfoRestResponse masterUserInfo, UserInfoRestResponse responseUserInfo, List<String> updatedProductList
	) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("responseUserNickName", responseUserInfo.getNickname());
		context.setVariable("responseUserEmail", responseUserInfo.getEmail());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		context.setVariable("plan", org.apache.commons.lang.StringUtils.join(updatedProductList, ","));
		return context;
	}

	public Context getWorkspaceUserPermissionUpdateContext(
		String workspaceName, UserInfoRestResponse masterUserInfo, UserInfoRestResponse responseUserInfo, String role
	) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("responseUserNickName", responseUserInfo.getNickname());
		context.setVariable("responseUserEmail", responseUserInfo.getEmail());
		context.setVariable("role", role);
		context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;
	}

	public Context getWorkspaceKickoutContext(String workspaceName, UserInfoRestResponse masterUserInfo) {
		Context context = new Context();
		context.setVariable("workspaceName", workspaceName);
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		return context;
	}

	public Context getWorkspaceInfoUpdateContext(String oldWorkspaceName, String newWorkspaceName, UserInfoRestResponse masterUserInfo) {
		Context context = new Context();
		context.setVariable("beforeWorkspaceName", oldWorkspaceName);
		context.setVariable("afterWorkspaceName", newWorkspaceName);
		context.setVariable("supportUrl", redirectProperty.getSupportWeb());
		context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
		return context;
	}
}