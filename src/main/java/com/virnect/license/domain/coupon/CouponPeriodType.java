package com.virnect.license.domain.coupon;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
public enum CouponPeriodType {
    DAY("DAY"), // 일간
    WEEK("WEEK"), // 주간
    MONTH("MONTH"), // 월간
    YEAR("YEAR"); // 연간
    private String value;

    CouponPeriodType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
