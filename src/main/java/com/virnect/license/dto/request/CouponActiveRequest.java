package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Getter
@Setter
@ApiModel
public class CouponActiveRequest {
    @ApiModelProperty(value = "사용자 식별자", example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String userId;
    @ApiModelProperty(value = "사용자의 마스터 워크스페이스 식별자", example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String workspaceId;
    @ApiModelProperty(value = "내 미사용 쿠폰 아이디", example = "2")
    private long couponId;
}
