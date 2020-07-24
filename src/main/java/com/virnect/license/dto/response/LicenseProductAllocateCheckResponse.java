package com.virnect.license.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@ApiModel
public class LicenseProductAllocateCheckResponse {
    @ApiModelProperty(value = "사용자 대표 식별자", example = "0")
    private long userId;
    @ApiModelProperty(value = "상품 지급 인증 코드", position = 1, example = "48254844-235e-4421-b713-4ea682994a98")
    private String assignAuthCode;
    @ApiModelProperty(value = "상품 지급 여부 결과", position = 2, example = "true")
    private boolean isAssignable;
    @ApiModelProperty(value = "상품 지급 여부 검사 일자", position = 3, example = "2020-05-11T02:56:35")
    private LocalDateTime assignableCheckDate;
}
