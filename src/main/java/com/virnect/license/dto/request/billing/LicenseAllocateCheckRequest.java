package com.virnect.license.dto.request.billing;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@ApiModel
public class LicenseAllocateCheckRequest {
    @ApiModelProperty(value = "사용자 대표 식별자")
    @NotNull(message = "사용자 대표 식별자는 반드시 있어야 합니다.")
    @Positive(message = "사용자 대표 식별자는 0보다 커야 합니다.")
    private long userId;
    @ApiModelProperty(value = "정기 결제 요청 여부(최초 요청 건 or 정기 결제 요청 건)", position = 1, example = "false")
    @NotNull(message = "regularRequest 값은 반드시 있어야 합니다.")
    private boolean regularRequest;
    @ApiModelProperty(value = "상품 정보 리스트", position = 2)
    @NotNull(message = "상품 정보는 반드시 있어야 합니다.")
    @Size(min = 1, message = "상품 정보는 반드시 하나 이상 존재 해야 합니다.")
    private List<LicenseAllocateProductInfoResponse> productList;
    @ApiModelProperty(value = "쿠폰 정보 리스트", position = 3)
    private List<LicenseAllocateCouponInfoResponse> couponList;

    @Override
    public String toString() {
        return "LicenseAllocateCheckRequest{" +
                "userId=" + userId +
                ", regularRequest=" + regularRequest +
                ", productList=" + productList +
                ", couponList=" + couponList +
                '}';
    }
}
