package com.virnect.license.dto.billing.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingProductInfoResponse {
	@JsonProperty(value = "ItemID")
	private long itemId;
	@JsonProperty(value = "ItemName")
	private String itemName;
	@JsonProperty(value = "ItemDesc")
	private String itemDesc;
	@JsonProperty(value = "ItemNameEng")
	private String itemNameEng;
	@JsonProperty(value = "ItemDescEng")
	private String itemDescEng;
	@JsonProperty(value = "SalesStartYMD")
	private String salesStartYMD;
	@JsonProperty(value = "SalesEndYMD")
	private String salesEndYMD;
	@JsonProperty(value = "KRWAmt")
	private long KRWAmt;
	@JsonProperty(value = "USDAmt")
	private long USDAmt;
	@JsonProperty(value = "CPItemID")
	private String CPItemId;
	private long productHit;
	private long productStorage;
	private long productCallTime;
	private BillingProductTypeInfoResponse productType;

	@Override
	public String toString() {
		return "BillingProductInfoResponse{" +
			"itemId=" + itemId +
			", itemName='" + itemName + '\'' +
			", itemDesc='" + itemDesc + '\'' +
			", itemNameEng='" + itemNameEng + '\'' +
			", itemDescEng='" + itemDescEng + '\'' +
			", salesStartYMD='" + salesStartYMD + '\'' +
			", salesEndYMD='" + salesEndYMD + '\'' +
			", KRWAmt=" + KRWAmt +
			", USDAmt=" + USDAmt +
			", CPItemId='" + CPItemId + '\'' +
			", productHit=" + productHit +
			", productStorage=" + productStorage +
			", productCallTime=" + productCallTime +
			", productType=" + productType +
			'}';
	}
}
