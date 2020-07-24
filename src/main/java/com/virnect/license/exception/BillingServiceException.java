package com.virnect.license.exception;

import com.virnect.license.global.error.ErrorCode;

public class BillingServiceException extends RuntimeException {
    private final ErrorCode error;

    public BillingServiceException(ErrorCode error) {
        this.error = error;
    }

    public ErrorCode getError() {
        return error;
    }
}
