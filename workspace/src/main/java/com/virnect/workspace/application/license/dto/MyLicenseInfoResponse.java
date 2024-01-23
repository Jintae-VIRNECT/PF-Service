package com.virnect.workspace.application.license.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.workspace.domain.rest.LicenseStatus;

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
public class MyLicenseInfoResponse {
	@ApiModelProperty(value = "라이선스 식별자", example = "1")
	private long id = 0L;
	@ApiModelProperty(value = "라이선스 시리얼 키", example = "29923633-47D7-439C-B515-94FF4A9B5BB0")
	private String serialKey = "";
	@ApiModelProperty(value = "라이선스 상태")
	private LicenseStatus status;
	@ApiModelProperty(value = "라이선스 할당 제품명", example = "MAKE")
	private String productName = "";
	@ApiModelProperty(value = "라이선스 타입", example = "BASIC")
	private String licenseType = "";
	@ApiModelProperty(value = "라이선스 정보 생성일", example = "2020-04-16T16:34:35")
	private LocalDateTime createdDate = LocalDateTime.now();
	@ApiModelProperty(value = "라이선스 정보 수정일", example = "2020-04-16T16:34:35")
	private LocalDateTime updatedDate = LocalDateTime.now();
}
