package com.virnect.license.dto.billing.request;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class LicenseProductDeallocateRequest {
	@NotNull
	private long userId;
	@NotNull
	private String paymentId;
	@NotNull
	private LocalDateTime paymentDate;
	@NotBlank
	private String operatedBy;

	@Override
	public String toString() {
		return "LicenseProductDeallocateRequest{" +
			"userId=" + userId +
			", paymentId=" + paymentId +
			", paymentDate=" + paymentDate +
			", operatedBy='" + operatedBy + '\'' +
			'}';
	}
}
