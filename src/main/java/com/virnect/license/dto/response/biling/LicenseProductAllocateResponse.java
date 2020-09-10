package com.virnect.license.dto.response.biling;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.license.dto.request.billing.AllocateProductInfoResponse;

@Getter
@Setter
@ApiModel
@NoArgsConstructor
public class LicenseProductAllocateResponse {
	@ApiModelProperty(value = "사용자 대표 식별자")
	private long userId;
	@ApiModelProperty(value = "결제 정보 식별자")
	private String paymentId;
	@ApiModelProperty(value = "상품 지급 일자")
	private LocalDateTime allocatedDate;
	@ApiModelProperty(value = "지급 상품 정보")
	private List<AllocateProductInfoResponse> allocatedProductList;

	@Builder
	public LicenseProductAllocateResponse(
		final long userId, final String paymentId, final List<AllocateProductInfoResponse> allocatedProductList,
		LocalDateTime allocatedDate
	) {
		this.userId = userId;
		this.paymentId = paymentId;
		this.allocatedProductList = allocatedProductList;
		this.allocatedDate = allocatedDate;
	}

	@Override
	public String toString() {
		return "LicenseProductAllocateResponse{" +
			"userId=" + userId +
			", paymentId='" + paymentId + '\'' +
			", allocatedDate=" + allocatedDate +
			", allocatedProductList=" + allocatedProductList +
			'}';
	}
}
