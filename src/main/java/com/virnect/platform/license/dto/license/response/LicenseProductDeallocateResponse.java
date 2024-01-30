package com.virnect.platform.license.dto.license.response;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class LicenseProductDeallocateResponse {
	private long userId;
	private String paymentId;
	private LocalDateTime deallocatedDate;

	@Override
	public String toString() {
		return "LicenseProductDeallocateResponse{" +
			"userId=" + userId +
			", paymentId=" + paymentId +
			", deallocatedDate=" + deallocatedDate +
			'}';
	}
}
