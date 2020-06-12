package com.virnect.license.dto.request;

import com.virnect.license.domain.ProductDisplayStatus;
import com.virnect.license.validator.ValueOfEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class ProductInfoUpdateRequest {
    @NotNull
    @ApiModelProperty(value = "상품 고유 식별자")
    private long productId;
    @ApiModelProperty(value = "상품명")
    private String productName;
    @ApiModelProperty(value = "상품 가격")
    private long productPrice;
    @ApiModelProperty(value = "상품 기본 최대 저장 용량")
    private long productMaxStorageSize;
    @ApiModelProperty(value = "상품 기본 최대 다운로드 가능 횟수")
    private long productMaxDownloadHit;
    @ApiModelProperty(value = "상품 기본 최대 통화 시간")
    private long productMaxCallTime;
    @ApiModelProperty(value = "상품 타입 고유 식별자")
    private long productTypeId;
    @ApiModelProperty(value = "상품 표시 여부")
    @ValueOfEnum(enumClass = ProductDisplayStatus.class)
    private String productDisplayStatus;
}
