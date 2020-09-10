package com.virnect.license.dto.response.biling;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@RequiredArgsConstructor
public class ProductTypeInfoListResponse {
	@ApiModelProperty(value = "상품 타입 정보 리스트")
	private final List<ProductTypeInfoResponse> productTypeInfoList;
}
