package com.virnect.license.dto.rest.billing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingProductTypeInfo {
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
