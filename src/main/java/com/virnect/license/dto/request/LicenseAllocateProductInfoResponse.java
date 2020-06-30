package com.virnect.license.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class LicenseAllocateProductInfoResponse {
    @ApiModelProperty(value = "상품 대표 식별자", example = "1001")
    private long productId;
    @ApiModelProperty(value = "상품명", position = 1, example = "VIRNECT Remote Basic Plan 1인 사용권")
    private String productName;
    @ApiModelProperty(value = "상품 수량", position = 2, example = "2")
    private int productAmount;
    @ApiModelProperty(value = "지급 다운로드 횟수", position = 3, example = "0")
    private long productHit;
    @ApiModelProperty(value = "지급 스토리지 용량", position = 4, example = "0")
    private long productStorage;
    @ApiModelProperty(value = "지급 통화 시간", position = 5, example = "30")
    private long productCallTime;
    @ApiModelProperty(value = "상품 타입 정보", position = 6)
    private ProductTypeRequest productType;

    @Override
    public String toString() {
        return "LicenseAllocateProductInfoResponse{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productAmount=" + productAmount +
                ", productHit=" + productHit +
                ", productStorage=" + productStorage +
                ", productCallTime=" + productCallTime +
                ", productType=" + productType +
                '}';
    }
}
