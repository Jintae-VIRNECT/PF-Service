package com.virnect.license.dto.request;


import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
public class LicenseProductDeallocateRequest {
    @NotNull
    private long userId;
    @NotNull
    private long paymentId;
    @NotNull
    private LocalDateTime paymentDate;
    @NotBlank
    private String operatedBy;

    @Override
    public String toString() {
        return "LicenseProductDeallocateRequest{" +
                "userId=" + userId +
                ", paymentId=" + paymentId +
                ", paymentDate=" + paymentDate +
                ", operatedBy='" + operatedBy + '\'' +
                '}';
    }
}
