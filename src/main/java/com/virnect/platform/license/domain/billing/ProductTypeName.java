package com.virnect.platform.license.domain.billing;

public enum ProductTypeName {
	REMOTE("Remote"),
	MAKE("Make"),
	VIEW("View"),
	CALL_TIME("CallTime"),
	STORAGE("Storage"),
	HIT("Hit");

	private final String value;

	ProductTypeName(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public boolean is(String target) {
		return this.value.equals(target);
	}
}
