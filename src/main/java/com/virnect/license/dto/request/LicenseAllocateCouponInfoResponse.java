package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
public class LicenseAllocateCouponInfoResponse {
    @ApiModelProperty(value = "쿠폰 식별자(시리얼 코드)", example = "AXD5-RFDS-ASDC-AAAS")
    private String couponId;
    @ApiModelProperty(value = "쿠폰 명", position = 1, example = "20% 할인 쿠폰")
    private String couponName;
    @ApiModelProperty(value = "생성 일자", position = 2, example = "2020-05-03T18:00:11")
    private LocalDateTime createdDate;

    @Override
    public String toString() {
        return "LicenseAllocateCouponInfoResponse{" +
                "couponId='" + couponId + '\'' +
                ", couponName='" + couponName + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
