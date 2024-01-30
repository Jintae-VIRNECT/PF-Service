package com.virnect.platform.license.dto.sdk.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class SDKLicenseRegisterRequest {
	@NotBlank
	@Pattern(
		regexp = "[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}",
		message = "시리얼 키 값은 다음과 같은 형식이여야 합니다. aaaa-cccc-dddd-1ada"
	)
	@ApiModelProperty(value = "라이선스 시리얼 키 값 정보", example = "aaaa-cccc-dddd-1ada")
	private String serialKey;
	@NotBlank
	@Pattern(
		regexp = "^\\d{4}-\\d{2}-\\d{2}$",
		message = "시리얼 키 값은 다음과 같은 형식이여야 합니다. aaaa-cccc-dddd-1ada"
	)
	@ApiModelProperty(value = "라이선스 시작 일자 (YYYY-MM-DD) ISO DATE 포맷", position = 1, example = "2020-01-01")
	private String startDate;
	@NotBlank
	@Pattern(
		regexp = "^\\d{4}-\\d{2}-\\d{2}$",
		message = "시리얼 키 값은 다음과 같은 형식이여야 합니다. aaaa-cccc-dddd-1ada"
	)
	@ApiModelProperty(value = "라이선스 만료 일자 (YYYY-MM-DD) ISO DATE 포맷", position = 2, example = "2020-11-01")
	private String expiredDate;

	@Override
	public String toString() {
		return "SDKLicenseRegisterRequest{" +
			"serialKey='" + serialKey + '\'' +
			", startDate='" + startDate + '\'' +
			", expiredDate='" + expiredDate + '\'' +
			'}';
	}
}
