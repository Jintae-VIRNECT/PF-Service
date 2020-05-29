package com.virnect.license.dto.response.biling;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ProductInfoResponse {
    @ApiModelProperty(value = "상품 고유 식별자")
    private long productId;
    @ApiModelProperty(value = "상품명", position = 1)
    private String name;
    @ApiModelProperty(value = "상품 가격", position = 2)
    private long price;
    @ApiModelProperty(value = "상품 기본 최대 용량", position = 3)
    private long maxStorageSize;
    @ApiModelProperty(value = "상품 기본 최대 다운로드 횟수", position = 4)
    private long maxDownloadHit;
    @ApiModelProperty(value = "상품 기본 최대 통화 횟수", position = 5)
    private long maxCallTime;
    @ApiModelProperty(value = "상품 타입 고유 식별자", position = 6)
    private long productTypeId;
    @ApiModelProperty(value = "상품 타입명", position = 7)
    private String productTypeName;
}
