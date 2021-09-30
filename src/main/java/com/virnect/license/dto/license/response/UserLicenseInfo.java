package com.virnect.license.dto.license.response;

import java.time.LocalDateTime;

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
