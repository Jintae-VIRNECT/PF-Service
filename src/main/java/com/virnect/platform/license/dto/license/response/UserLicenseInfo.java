package com.virnect.platform.license.dto.license.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLicenseInfo {
	private String userId;
	private long licenseId;
	private String serialKey;
	private String productName;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;

	public static Map<String, List<String>> getProductLicenseGroupedByUser(
		List<UserLicenseInfo> userLicenseInfoList
	) {
		return userLicenseInfoList.stream()
			.collect(Collectors.groupingBy(
				UserLicenseInfo::getUserId,
				Collectors.mapping(UserLicenseInfo::getProductName, Collectors.toList())
			));
	}

	@Override
	public String toString() {
		return "UserLicenseInfo{" +
			", userId='" + userId + '\'' +
			", licenseId=" + licenseId +
			", serialKey='" + serialKey + '\'' +
			", productName='" + productName + '\'' +
			", createdDate=" + createdDate +
			", updatedDate=" + updatedDate +
			'}';
	}
}
