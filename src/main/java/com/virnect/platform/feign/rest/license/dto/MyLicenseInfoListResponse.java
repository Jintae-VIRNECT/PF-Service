package com.virnect.platform.feign.rest.license.dto;

import java.util.List;

import org.springframework.util.CollectionUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-05-28
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@ApiModel
public class MyLicenseInfoListResponse {
	@ApiModelProperty(value = "내 라이선스 정보 목록")
	private List<MyLicenseInfoResponse> licenseInfoList;

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(licenseInfoList);
	}

	public boolean isHaveRemoteLicense() {
		return !CollectionUtils.isEmpty(licenseInfoList) &&
			licenseInfoList.stream().anyMatch(myLicenseInfoResponse -> myLicenseInfoResponse.getProductName().equals("REMOTE"));

	}
}
