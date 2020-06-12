package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CreateNewProductTypeRequest {
    private String productTypeName;
}
