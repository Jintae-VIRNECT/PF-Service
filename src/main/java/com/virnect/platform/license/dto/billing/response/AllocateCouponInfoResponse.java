package com.virnect.platform.license.dto.billing.response;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.platform.license.domain.billing.CouponPeriodType;
import com.virnect.platform.license.validator.ValueOfEnum;

@Getter
@Setter
@ApiModel
public class AllocateCouponInfoResponse {
	@ApiModelProperty(value = "쿠폰 식별자(시리얼 코드)", example = "AXD5-RFDS-ASDC-AAAS")
	private String couponId;
	@ApiModelProperty(value = "쿠폰 명", position = 1, example = "20% 할인 쿠폰")
	private String couponName;
	@ApiModelProperty(value = "생성 일자", position = 2, example = "2020-05-03T18:00:11")
	private LocalDateTime createdDate;
	@ApiModelProperty(value = "쿠폰 기간 타입", position = 3, example = "NONE")
	@ValueOfEnum(enumClass = CouponPeriodType.class)
	private CouponPeriodType periodType;
	@ApiModelProperty(value = "쿠폰 기간", position = 4, example = "0")
	private int period;

	@Override
	public String toString() {
		return "AllocateCouponInfoResponse{" +
			"couponId='" + couponId + '\'' +
			", couponName='" + couponName + '\'' +
			", createdDate=" + createdDate +
			", periodType=" + periodType +
			", period=" + period +
			'}';
	}
}
