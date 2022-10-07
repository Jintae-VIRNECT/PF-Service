package com.virnect.workspace.application.license.dto;

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
}