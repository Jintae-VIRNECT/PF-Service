package com.virnect.license.dto.license.response;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserLicenseInfoResponse {
	private final List<UserLicenseInfo> userLicenseInfos;
	private final String workspaceId;

	@Override
	public String toString() {
		return "UserLicenseInfoResponse{" +
			"userLicenseInfos=" + userLicenseInfos +
			", workspaceId='" + workspaceId + '\'' +
			'}';
	}
}
