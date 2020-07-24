package com.virnect.license.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class LicenseProductInfoResponse {
    @ApiModelProperty(value = "제품 식별자", example = "1")
    private long productId;
    @ApiModelProperty(value = "제품명", position = 1, example = "MAKE")
    private String productName;
    @ApiModelProperty(value = "제품 라이선스 타입", position = 2, example = "BASIC")
    private String licenseType;
    @ApiModelProperty(value = "재품 라이선스 수량", position = 3, example = "4")
    private int quantity;
    @ApiModelProperty(value = "현재 할당 가능한 라이선스 수량", position = 4, example = "1")
    private int unUseLicenseAmount;
    @ApiModelProperty(value = "현재 할당 된 라이선스 수량", position = 5, example = "1")
    private int useLicenseAmount;
    @ApiModelProperty(value = "라이선스 정보", position = 6)
    private List<LicenseInfoResponse> licenseInfoList;
}
