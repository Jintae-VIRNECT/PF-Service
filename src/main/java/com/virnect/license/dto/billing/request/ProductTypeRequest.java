package com.virnect.license.dto.billing.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ProductTypeRequest {
	@ApiModelProperty(value = "상품 타입 식별자", example = "product")
	private String id;
	@ApiModelProperty(value = "상품명", position = 1, example = "Remote")
	private String name;

	@Override
	public String toString() {
		return "ProductTypeRequest{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
