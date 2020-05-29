package com.virnect.license.exception;


import com.virnect.license.global.error.ErrorCode;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-Auth
 * @email practice1356@gmail.com
 * @description Login Fail Exception Class
 * @since 2020.05.08
 */

public class LicenseAllocateDeniedException extends RuntimeException {
    private final ErrorCode error;
    private final long userId;
    private final boolean isAssignable;

    public LicenseAllocateDeniedException(ErrorCode error, long userId) {
        super(error.getMessage());
        this.error = error;
        this.userId = userId;
        this.isAssignable = false;
    }

    public ErrorCode getError() {
        return error;
    }

    public long getUserId() {
        return userId;
    }

    public boolean isAssignable() {
        return isAssignable;
    }
}
