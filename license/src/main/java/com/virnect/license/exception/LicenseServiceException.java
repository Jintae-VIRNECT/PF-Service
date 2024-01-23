package com.virnect.license.exception;

import com.virnect.license.global.error.ErrorCode;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description LicenseServiceException
 * @since 2020.04.09
 */
public class LicenseServiceException extends RuntimeException {
	private final ErrorCode error;

	public LicenseServiceException(ErrorCode error) {
		this.error = error;
	}

	public ErrorCode getError() {
		return error;
	}
}
