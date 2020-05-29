package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class LicenseAllocateProductInfoResponse {
    @ApiModelProperty(value = "상품 대표 식별자")
    private long productId;
    @ApiModelProperty(value = "상품명")
    private String productName;
    @ApiModelProperty(value = "상품 수량")
    private int productAmount;
}
