package com.virnect.license.dto.request.billing;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
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
public class LicenseProductAllocateRequest {
	@NotNull(message = "사용자 대표 식별자 정보는 반드시 값이 있어야 합니다.")
	@Positive(message = "사용자 대표 식별자는 0보다 커야 합니다.")
	@ApiModelProperty(value = "사용자 대표 식별자", example = "1")
	private long userId;
	@ApiModelProperty(value = "정기 결제 요청 여부(최초 요청 건 or 정기 결제 요청 건)", position = 1, example = "false")
	@NotNull(message = "정기 결제 요청 여부 정보는 반드시 값이 있어야 합니다.")
	private boolean regularRequest;
	@ApiModelProperty(value = "기간 결제 요청 여부(일반 구독 결제 건 or 기간 결제 건)", position = 2, example = "false")
	@NotNull(message = "기간 결제 요청 여부 정보는 반드시 값이 있어야 합니다.")
	private boolean termPaymentRequest;
	@NotBlank(message = "결제 정보 식별자 정보는 반드시 값이 있어야 합니다.")
	@ApiModelProperty(value = "결제 정보 식별자", position = 3, example = "202019239941")
	private String paymentId;
	@NotNull(message = "결제 일자 정보는 반드시 값이 있어야 합니다.")
	@ApiModelProperty(value = "결제 일자", position = 4, example = "2020-06-03T18:00:11")
	private LocalDateTime paymentDate;
	@NotBlank(message = "결제 국가 코드 정보는 반드시 값이 있어야 합니다.")
	@ApiModelProperty(value = "결제 국가 코드", position = 5, example = "KR")
	private String userCountryCode;
	@NotBlank(message = "라이선스 지급 인증 코드 정보는 반드시 값이 있어야 합니다.")
	@ApiModelProperty(value = "지급 인증 코드", position = 6, example = "48254844-235e-4421-b713-4ea682994a98")
	private String assignAuthCode;
	@ApiModelProperty(value = "결제 상품 정보", position = 7)
	@NotNull(message = "상품 정보는 반드시 값이 있어야 합니다.")
	@Size(min = 1, message = "상품 정보는 반드시 하나 이상 있어야 합니다.")
	private List<AllocateProductInfoResponse> productList;
	@ApiModelProperty(value = "결제에 사용된 쿠폰 정보", position = 8)
	private List<AllocateCouponInfoResponse> couponList;

	@Override
	public String toString() {
		return "LicenseProductAllocateRequest{" +
			"userId=" + userId +
			", regularRequest=" + regularRequest +
			", termPaymentRequest=" + termPaymentRequest +
			", paymentId='" + paymentId + '\'' +
			", paymentDate=" + paymentDate +
			", userCountryCode='" + userCountryCode + '\'' +
			", assignAuthCode='" + assignAuthCode + '\'' +
			", productList=" + productList +
			", couponList=" + couponList +
			'}';
	}
}
