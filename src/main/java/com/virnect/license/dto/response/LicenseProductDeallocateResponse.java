package com.virnect.license.dto.response;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
public class LicenseProductDeallocateResponse {
    private long userId;
    private String paymentId;
    private LocalDateTime deallocatedDate;

    @Override
    public String toString() {
        return "LicenseProductDeallocateResponse{" +
                "userId=" + userId +
                ", paymentId=" + paymentId +
                ", deallocatedDate=" + deallocatedDate +
                '}';
    }
}
