package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class LicenseAllocateCheckRequest {
    @ApiModelProperty(value = "사용자 대표 식별자")
    private long userId;
    @ApiModelProperty(value = "상품 정보 리스트")
    private List<LicenseAllocateProductInfoResponse> productList;
    @ApiModelProperty(value = "쿠폰 정보 리스트")
    private List<LicenseAllocateCouponInfoResponse> couponList;
}
