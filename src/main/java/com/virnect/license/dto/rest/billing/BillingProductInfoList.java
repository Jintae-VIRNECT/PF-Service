package com.virnect.license.dto.rest.billing;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingProductInfoList {
	private List<BillingProductInfo> products;

	@Override
	public String toString() {
		return "BillingProductInfoList{" +
			"products=" + products +
			'}';
	}
}
