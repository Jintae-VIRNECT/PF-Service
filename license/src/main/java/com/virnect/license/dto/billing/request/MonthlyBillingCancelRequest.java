package com.virnect.license.dto.billing.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyBillingCancelRequest {
	@JsonProperty(value = "sitecode")
	private long siteCode;
	@JsonProperty(value = "userno")
	private long userNumber;
	@JsonProperty(value = "MSeqNo")
	private int userMonthlyBillingNumber;

	@Override
	public String toString() {
		return "MonthlyBillingCancelRequest{" +
			"siteCode=" + siteCode +
			", userNumber=" + userNumber +
			", userMonthlyBillingNumber=" + userMonthlyBillingNumber +
			'}';
	}
}
