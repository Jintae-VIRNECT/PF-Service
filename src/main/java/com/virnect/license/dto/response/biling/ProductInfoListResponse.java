package com.virnect.license.dto.response.biling;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
@RequiredArgsConstructor
public class ProductInfoListResponse {
    @ApiModelProperty(value = "상품 정보 리스트")
    private final List<ProductInfoResponse> productInfoList;
}
