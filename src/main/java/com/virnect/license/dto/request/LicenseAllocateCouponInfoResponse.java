package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
public class LicenseAllocateCouponInfoResponse {
    private long couponId;
    private String couponName;
    private LocalDateTime createdDate;
}
