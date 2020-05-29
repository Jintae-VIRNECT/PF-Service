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
    @ApiModelProperty(value = "사용자 대표 식별자")
    private long userId;
    @ApiModelProperty(value = "상품 지급 여부 결과")
    private boolean isAssignable;
    @ApiModelProperty(value = "상품 지급 여부 검사 일자")
    private LocalDateTime assignableCheckDate;
}
