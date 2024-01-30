package com.virnect.platform.license.dto.billing.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ProductInfoResponse {
	@ApiModelProperty(value = "상품 고유 식별자", example = "1")
	private long id;
	@ApiModelProperty(value = "빌링 시스템 상품 고유 식별자", position = 1, example = "1001")
	private long billProductId;
	@ApiModelProperty(value = "상품명", position = 2, example = "테스트 상품")
	private String name;
	@ApiModelProperty(value = "상품 기본 최대 용량", position = 3, example = "0")
	private long maxStorageSize;
	@ApiModelProperty(value = "상품 기본 최대 다운로드 횟수", position = 4, example = "0")
	private long maxDownloadHit;
	@ApiModelProperty(value = "상품 기본 최대 통화 횟수", position = 5, example = "0")
	private long maxCallTime;
	@ApiModelProperty(value = "상품 타입 고유 식별자", position = 6, example = "6")
	private long productTypeId;
	@ApiModelProperty(value = "상품 타입명", position = 7, example = "product")
	private String productTypeName;

	@Override
	public String toString() {
		return "ProductInfoResponse{" +
			"id=" + id +
			", billProductId=" + billProductId +
			", name='" + name + '\'' +
			", maxStorageSize=" + maxStorageSize +
			", maxDownloadHit=" + maxDownloadHit +
			", maxCallTime=" + maxCallTime +
			", productTypeId=" + productTypeId +
			", productTypeName='" + productTypeName + '\'' +
			'}';
	}
}
