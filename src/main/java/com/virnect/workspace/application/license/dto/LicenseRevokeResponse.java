package com.virnect.workspace.application.license.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-License
 * DATE: 2021-08-18
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@ApiModel
public class LicenseRevokeResponse {
	@ApiModelProperty(value = "라이선스 해제 처리 결과", example = "true")
	private boolean result;
	@ApiModelProperty(value = "라이선스 해제 일자", position = 1, example = "2020-01-20T14:05:30")
	private LocalDateTime deletedDate;

	@Override
	public String toString() {
		return "LicenseRevokeResponse{" +
			"result=" + result +
			", deletedDate=" + deletedDate +
			'}';
	}
}
