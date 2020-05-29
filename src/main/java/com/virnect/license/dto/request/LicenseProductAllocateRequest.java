package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ApiModel
public class LicenseProductAllocateRequest {
    private long userId;
    private long paymentId;
    private LocalDateTime paymentDate;
    private String userCountryCode;
    private List<LicenseAllocateProductInfoResponse> productList;
    private List<LicenseAllocateCouponInfoResponse> couponList;
}
