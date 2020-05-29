package com.virnect.license.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.20
 */
@Getter
@Setter
@ApiModel
@RequiredArgsConstructor
public class MyLicenseInfoListResponse {
    @ApiModelProperty(value = "내 라이선스 정보 목록")
    private final List<MyLicenseInfoResponse> licenseInfoList;
}
