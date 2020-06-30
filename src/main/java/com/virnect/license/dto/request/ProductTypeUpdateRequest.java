package com.virnect.license.dto.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@ApiModel
public class ProductTypeUpdateRequest {
    @NotNull
    @Positive
    @ApiModelProperty(value = "상품 타입 고유 식별자", example = "1")
    private long productTypeId;
    @NotBlank
    @ApiModelProperty(value = "수정하고자 하는 상품 타입 명", position = 1, example = "BASIC")
    private String productTypeName;
}
