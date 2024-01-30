package com.virnect.platform.license.dto.billing.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingRestResult {
	private int code;
	private String message;
	private String detail;

	@Override
	public String toString() {
		return "BillingRestResult{" +
			"code=" + code +
			", message='" + message + '\'' +
			", detail='" + detail + '\'' +
			'}';
	}
}
