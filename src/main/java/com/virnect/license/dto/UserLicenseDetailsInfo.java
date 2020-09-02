package com.virnect.license.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserLicenseDetailsInfo {
	private final String workspaceId;
	private final String productName;
	private final LocalDateTime endDate;

	@Override
	public String toString() {
		return "UserLicenseDetailsInfo{" +
			"workspaceId='" + workspaceId + '\'' +
			", productName='" + productName + '\'' +
			", endDate=" + endDate +
			'}';
	}
}
