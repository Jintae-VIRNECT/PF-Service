package com.virnect.license.domain.billing;

public enum ProductTypeId {
	PRODUCT("product", "BASIC PLAN"),
	SERVICE("service", "ADD SERVICE");

	private final String value;
	private final String plan;

	ProductTypeId(String value, String plan) {
		this.value = value;
		this.plan = plan;
	}

	public String getValue() {
		return value;
	}

	public String getPlan() {
		return plan;
	}
}
