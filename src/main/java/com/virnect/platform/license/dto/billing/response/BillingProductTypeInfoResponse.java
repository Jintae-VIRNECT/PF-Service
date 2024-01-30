package com.virnect.platform.license.dto.billing.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingProductTypeInfoResponse {
	private String id;
	private String name;

	@Override
	public String toString() {
		return "BillingProductTypeInfo{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
