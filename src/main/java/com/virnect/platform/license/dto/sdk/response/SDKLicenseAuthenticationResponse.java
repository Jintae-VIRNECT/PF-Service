package com.virnect.platform.license.dto.sdk.response;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.platform.license.domain.applicense.AppLicenseStatus;

@Getter
@Setter
@ApiModel
public class SDKLicenseAuthenticationResponse {
	@ApiModelProperty(value = "라이선스 검증 결과", example = "true")
	private boolean validationResult;
	@ApiModelProperty(value = "라이선스 상태 정보(USE:활성화, UNUSED:비활성화, TERMINATE:만료)", position = 1, example = "ACTIVE")
	private AppLicenseStatus status;
	@ApiModelProperty(value = "라이선스 활성화 일자", position = 2, example = "2020-05-11T02:56:35")
	private LocalDateTime startDate;
	@ApiModelProperty(value = "라이선스 만료 일자", position = 3, example = "2020-30-11T02:56:35")
	private LocalDateTime expiredDate;

	@Override
	public String toString() {
		return "SDKLicenseAuthenticationResponse{" +
			"validationResult=" + validationResult +
			", status=" + status +
			", startDate=" + startDate +
			", expiredDate=" + expiredDate +
			'}';
	}
}
