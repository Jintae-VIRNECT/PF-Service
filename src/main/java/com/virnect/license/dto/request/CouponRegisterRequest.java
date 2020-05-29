package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Getter
@Setter
@ApiModel
public class CouponRegisterRequest {
    @NotBlank
    @ApiModelProperty(value = "사용자 식별 번호", example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String userId;
    @NotBlank
    @ApiModelProperty(value = "쿠폰 시리얼 번호", example = "02851F30-B0FE-47EE-B049-CB4BEDB2F20A")
    private String couponSerialKey;
}
