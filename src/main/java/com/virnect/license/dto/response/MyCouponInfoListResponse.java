package com.virnect.license.dto.response;

import com.virnect.license.global.common.PageMetadataResponse;
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
 * @since 2020.04.09
 */
@Getter
@ApiModel
@RequiredArgsConstructor
public class MyCouponInfoListResponse {
    @ApiModelProperty(value = "내 쿠폰 정보 리스트")
    private final List<MyCouponInfoResponse> myCouponInfoList;
    @ApiModelProperty(value = "페이징 정보")
    private final PageMetadataResponse pageMeta;

    @Override
    public String toString() {
        return "MyCouponInfoListResponse{" +
                "myCouponInfoList=" + myCouponInfoList +
                ", pageMeta=" + pageMeta +
                '}';
    }
}
