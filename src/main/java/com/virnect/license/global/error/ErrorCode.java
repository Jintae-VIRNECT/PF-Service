package com.virnect.license.global.error;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Project: base
 * DATE: 2020-01-07
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@ApiModel
public enum ErrorCode {
    // 쿠폰 생성
    ERR_CREATE_COUPON(1000, "Coupon Generate fail."),
    ERR_ALREADY_REGISTER_EVENT_COUPON(1001, "Already event coupon is registered"),

    // 쿠폰 등록 및 사용
    ERR_COUPON_NOT_FOUND(2000, "Coupon not found by serial key"),
    ERR_COUPON_REGISTER_ALREADY_REGISTER(2001, "Coupon is already register"),
    ERR_COUPON_EXPIRED(2002, "Coupon is expired"),
    ERR_COUPON_ALREADY_ACTIVATED(2003, "Coupon is already activated"),


    // 라이선스 조회 관련
    ERR_LICENSE_PLAN_NOT_FOUND(3000, "License plan not found"),
    ERR_LICENSE_PRODUCT_NOT_FOUND(3001, "License Product not found"),

    // 상품 라이선스 등록 관련
    ERR_PRODUCT_LICENSE_ASSIGNMENT_FROM_PAYMENT(4000, "Product license assignment failed."),
    ERR_PRODUCT_LICENSE_ASSIGNMENT_ROLLBACK_FROM_PAYMENT(4100, "Product license deallocation failed."),

    // 상품 지급 여부 검사
    ERR_BILLING_PRODUCT_ALLOCATE_DENIED(4200, "License allocation imposible"),

    // 상품 조회 관련
    ERR_BILLING_PRODUCT_NOT_FOUND(4300, "Product Not found."),
    ERR_PRODUCT_INFO_UPDATE(4301, "Product info update faild."),


    // 라이선스 할당 관련
    ERR_LICENSE_ALREADY_GRANTED(5000, "License is already granted"),
    ERR_USEFUL_LICENSE_NOT_FOUND(5001, "Useful License not found"),


    // 페이레터
    ERR_BILLING_LICENSE_SERVER_ERROR(7777, "License Server error rollback process begin."),

    // 공통 에러
    ERR_API_AUTHENTICATION(8003, "Authentication Error"),

    ERR_INVALID_VALUE(8004, "Invalid Value"),

    ERR_INVALID_REQUEST_PARAMETER(8001, "Invalid request parameter cause api errors"),

    ERR_UNEXPECTED_SERVER_ERROR(9999, "Unexpected Server Error, Please contact Administrator");

    @ApiModelProperty(name = "code")
    private int code;
    @ApiModelProperty(name = "message")
    private String message;

    ErrorCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
