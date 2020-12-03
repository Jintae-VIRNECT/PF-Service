package com.virnect.license.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.license.domain.applicense.AppLicenseStatus;

@Getter
@Setter
@ApiModel
public class SDKLicenseInfoResponse {
	@ApiModelProperty(value = "라이선스 아이디", example = "1")
	private long id;
	@ApiModelProperty(value = "라이선스 시리얼 키 정보", position = 1, example = "aaaa-bbbb-cccc-dddd")
	private String serialKey;
	@ApiModelProperty(value = "라이선스 상태 정보(USE:활성화, UNUSED:비활성화, TERMINATE:만료)", position = 2, example = "ACTIVE")
	private AppLicenseStatus status;
	@ApiModelProperty(value = "라이선스 활성화 일자", position = 3, example = "2020-05-11T02:56:35")
	private LocalDateTime startDate;
	@ApiModelProperty(value = "라이선스 만료 일자", position = 4, example = "2020-30-11T02:56:35")
	private LocalDateTime expiredDate;
	@ApiModelProperty(value = "라이선스 생성 일자", position = 5, example = "2020-30-11T02:56:35")
	private LocalDateTime createdDate;
	@ApiModelProperty(value = "라이선스 수정 일자", position = 6, example = "2020-30-11T02:56:35")
	private LocalDateTime updatedDate;
}
