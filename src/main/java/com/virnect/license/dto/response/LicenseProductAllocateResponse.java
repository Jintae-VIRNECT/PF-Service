package com.virnect.license.dto.response;

import com.virnect.license.dto.request.LicenseAllocateProductInfoResponse;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ApiModel
public class LicenseProductAllocateResponse {
    @ApiModelProperty(value = "사용자 대표 식별자")
    private long userId;
    @ApiModelProperty(value = "결제 정보 식별자")
    private long paymentId;
    @ApiModelProperty(value = "상품 지급 일자")
    private LocalDateTime allocatedDate;
    @ApiModelProperty(value = "지급 상품 정보")
    private List<LicenseAllocateProductInfoResponse> allocatedProductList;
}
