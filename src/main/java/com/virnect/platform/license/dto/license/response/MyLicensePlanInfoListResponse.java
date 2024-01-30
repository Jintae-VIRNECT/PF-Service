package com.virnect.platform.license.dto.license.response;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.virnect.platform.global.common.PageMetadataResponse;

@Getter
@RequiredArgsConstructor
@ApiModel
public class MyLicensePlanInfoListResponse {
	@ApiModelProperty(value = "내가 사용중인 플랜 정보 목록")
	private final List<MyLicensePlanInfoResponse> myPlanInfoList;
	@ApiModelProperty(value = "페이징 정보", position = 1)
	private final PageMetadataResponse pageMeta;
}
