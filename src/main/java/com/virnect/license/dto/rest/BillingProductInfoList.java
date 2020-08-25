package com.virnect.license.dto.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
