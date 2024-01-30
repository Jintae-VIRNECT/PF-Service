package com.virnect.platform.license.dto.billing.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyBillingInfoResponse {
	@JsonProperty(value = "MSeqNo")
	private int monthlyBillingNumber;
	@JsonProperty(value = "PGName")
	private String PGName;
	@JsonProperty(value = "CurrPayDate")
	private String currentPayDate;
	@JsonProperty(value = "NextPayDate")
	private String nextPayDate;
	@JsonProperty(value = "TotalPayAmt")
	private long totalPayAmount;
	@JsonProperty(value = "PayFlag")
	private String paymentFlag;

	@Override
	public String toString() {
		return "MonthlyBillingInfo{" +
			"monthlyBillingNumber=" + monthlyBillingNumber +
			", PGName='" + PGName + '\'' +
			", currentPayDate='" + currentPayDate + '\'' +
			", nextPayDate='" + nextPayDate + '\'' +
			", totalPayAmount=" + totalPayAmount +
			", paymentFlag='" + paymentFlag + '\'' +
			'}';
	}
}
