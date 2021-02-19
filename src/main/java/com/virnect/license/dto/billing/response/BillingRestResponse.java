package com.virnect.license.dto.billing.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.license.dto.billing.response.BillingRestResult;

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
