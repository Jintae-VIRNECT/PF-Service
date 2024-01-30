package com.virnect.platform.feign.rest.license.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * Project        : PF-Workspace
 * DATE           : 2023-04-04
 * AUTHOR         : VIRNECT-JINTAE (Jintae Kim)
 * EMAIL          : jtkim@virnect.com
 * DESCRIPTION    :
 * ===========================================================
 * DATE            AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-04-04      VIRNECT-JINTAE          최초 생성
 */
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
}