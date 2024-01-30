package com.virnect.platform.license.exception;

import java.util.HashMap;
import java.util.List;

import com.virnect.platform.global.error.ErrorCode;

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
	private final List<HashMap<String, Object>> details;

	public LicenseAllocateDeniedException(ErrorCode error, long userId, List<HashMap<String, Object>> details) {
		super(error.getMessage());
		this.error = error;
		this.userId = userId;
		this.details = details;
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

	public List<HashMap<String, Object>> getDetails() {
		return details;
	}
}
