package com.virnect.platform.license.dto.billing.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillingRestResponse<T> {
	private T data;
	private BillingRestResult result;

	@Override
	public String toString() {
		return "BillingRestResponse{" +
			"data=" + data +
			", result=" + result +
			'}';
	}
}
