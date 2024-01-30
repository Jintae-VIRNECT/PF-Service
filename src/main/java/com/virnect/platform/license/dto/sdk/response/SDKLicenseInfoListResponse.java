package com.virnect.platform.license.dto.sdk.response;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.virnect.platform.global.common.PageMetadataResponse;

@Getter
@ApiModel
@RequiredArgsConstructor
public class SDKLicenseInfoListResponse {
	@ApiModelProperty(value = "라이선스 정보 목록")
	private final List<SDKLicenseInfoResponse> licenseInfoList;
	@ApiModelProperty(value = "페이징 정보", position = 1)
	private final PageMetadataResponse pageMeta;
}
