package com.virnect.license.dto.billing.response;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ProductTypeInfoResponse {
	@ApiModelProperty(value = "상품 타입 고유 식별자", example = "1")
	private long id;
	@ApiModelProperty(value = "상품 타입 명", position = 1, example = "product")
	private String name;
	@ApiModelProperty(value = "상품 타입 생성일", position = 2, example = "2020-01-14 07:55:47")
	private LocalDateTime createdDate;
	@ApiModelProperty(value = "상품 타입 수정일", position = 3, example = "2020-01-14 07:55:47")
	private LocalDateTime updatedDate;
}
