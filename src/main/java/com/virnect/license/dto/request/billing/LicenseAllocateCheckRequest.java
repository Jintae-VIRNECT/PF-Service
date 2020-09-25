package com.virnect.license.dto.request.billing;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

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
	@ApiModelProperty(value = "기간 결제 요청 여부(일반 구독 결제 건 or 기간 결제 건)", position = 2, example = "false")
	@NotNull(message = "기간 결제 요청 여부 정보는 반드시 값이 있어야 합니다.")
	private boolean termPaymentRequest;
	@ApiModelProperty(value = "상품 정보 리스트", position = 3)
	@NotNull(message = "상품 정보는 반드시 있어야 합니다.")
	@Size(min = 1, message = "상품 정보는 반드시 하나 이상 존재 해야 합니다.")
	private List<AllocateProductInfoResponse> productList;
	@ApiModelProperty(value = "쿠폰 정보 리스트", position = 4)
	private List<AllocateCouponInfoResponse> couponList;

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
