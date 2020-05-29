package com.virnect.license.dto.request;

import com.virnect.license.domain.ProductDisplayStatus;
import com.virnect.license.validator.ValueOfEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel
public class ProductInfoUpdateRequest {
    @NotNull
    @ApiModelProperty(value = "상품 고유 식별자")
    private long productId;
    @NotBlank
    @ApiModelProperty(value = "상품명")
    private String name;
    @ApiModelProperty(value = "상품 가격")
    private long price;
    @ApiModelProperty(value = "상품 기본 최대 저장 용량")
    private long maxStorageSize;
    @ApiModelProperty(value = "상품 기본 최대 다운로드 가능 횟수")
    private long maxDownloadHit;
    @ApiModelProperty(value = "상품 기본 최대 통화 시간")
    private long maxCallTime;
    @ApiModelProperty(value = "상품 타입 고유 식별자")
    private long productTypeId;
//    @ApiModelProperty(value = "상품 표시 여부")
//    @ValueOfEnum(enumClass = ProductDisplayStatus.class)
//    private String displayStatus;
}
