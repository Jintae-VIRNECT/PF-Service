package com.virnect.platform.license.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.virnect.platform.license.domain.product.LicenseProductStatus;

@Getter
@RequiredArgsConstructor
public class UserLicenseDetailsInfo {
	private final String workspaceId;
	private final String productName;
	private final LocalDateTime endDate;
	private final LicenseProductStatus productPlanStatus;

	@Override
	public String toString() {
		return "UserLicenseDetailsInfo{" +
			"workspaceId='" + workspaceId + '\'' +
			", productName='" + productName + '\'' +
			", endDate=" + endDate +
			", productPlanStatus=" + productPlanStatus +
			'}';
	}
}
