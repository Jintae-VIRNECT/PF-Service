package com.virnect.workspace.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class WorkspaceUserInfoResponse {
	private String uuid;
	private String email;
	private String name;
	private String description;
	private String profile;
	private String loginLock;
	private String userType;
	private String nickName;
	private Role role;
	private Long roleId;
	private String createdDate;
	private String updatedDate;
	private LocalDateTime joinDate;
	private String[] licenseProducts = new String[0];

	@Builder
	public WorkspaceUserInfoResponse(
		String uuid, String email, String name, String description, String profile, String loginLock,
		String userType, String nickName, Role role, Long roleId, String createdDate, String updatedDate,
		LocalDateTime joinDate, String[] licenseProducts
	) {
		this.uuid = uuid;
		this.email = email;
		this.name = name;
		this.description = description;
		this.profile = profile;
		this.loginLock = loginLock;
		this.userType = userType;
		this.nickName = nickName;
		this.role = role;
		this.roleId = roleId;
		this.createdDate = createdDate;
		this.updatedDate = updatedDate;
		this.joinDate = joinDate;
		this.licenseProducts = licenseProducts;
	}

	public static WorkspaceUserInfoResponse createWorkspaceUserInfoResponse(
		UserInfoRestResponse userInfoRestResponse,
		WorkspaceUserPermission workspaceUserPermission,
		String... licenseProduct
	) {
		return WorkspaceUserInfoResponse.builder()
			.uuid(userInfoRestResponse.getUuid())
			.email(userInfoRestResponse.getEmail())
			.name(userInfoRestResponse.getName())
			.description(userInfoRestResponse.getDescription())
			.profile(userInfoRestResponse.getProfile())
			.loginLock(userInfoRestResponse.getLoginLock())
			.userType(userInfoRestResponse.getUserType().name())
			.nickName(userInfoRestResponse.getNickname())
			.role(workspaceUserPermission.getWorkspaceRole().getRole())
			.createdDate(userInfoRestResponse.getCreatedDate().toString())
			.updatedDate(userInfoRestResponse.getUpdatedDate().toString())
			.joinDate(workspaceUserPermission.getCreatedDate())
			.licenseProducts(licenseProduct)
			.build();
	}

	public void setUserLicenseProductForWorkspaceUserInfoResponse(String[] productLicenseArray) {
		this.licenseProducts = productLicenseArray;
	}
}
