package com.virnect.platform.license.dto.product;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CreateNewProductTypeRequest {
	private String productTypeName;
}
