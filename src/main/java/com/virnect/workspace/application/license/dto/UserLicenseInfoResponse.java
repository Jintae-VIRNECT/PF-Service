package com.virnect.workspace.application.license.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserLicenseInfoResponse {
	private List<UserLicenseInfo> userLicenseInfos;
	private String workspaceId;
}