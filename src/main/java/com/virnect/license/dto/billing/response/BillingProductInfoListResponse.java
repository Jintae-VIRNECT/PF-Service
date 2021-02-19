package com.virnect.license.dto.billing.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingProductInfoListResponse {
	private List<BillingProductInfoResponse> products;

	@Override
	public String toString() {
		return "BillingProductInfoList{" +
			"products=" + products +
			'}';
	}
}
