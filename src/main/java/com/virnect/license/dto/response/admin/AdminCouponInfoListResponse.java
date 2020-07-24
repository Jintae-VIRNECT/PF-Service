package com.virnect.license.dto.response.admin;

import com.virnect.license.global.common.PageMetadataResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.05.07
 */

@Getter
@RequiredArgsConstructor
@ApiModel
public class AdminCouponInfoListResponse {
    @ApiModelProperty(value = "쿠폰 정보 리스트")
    private final List<AdminCouponInfoResponse> couponInfoList;
    @ApiModelProperty(value = "페이징 정보")
    private final PageMetadataResponse pageMeta;
}
