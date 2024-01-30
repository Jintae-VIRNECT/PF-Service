package com.virnect.platform.workspace.exception;

import com.virnect.platform.workspace.global.error.ErrorCode;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class WorkspaceException extends RuntimeException {

	private final ErrorCode errorCode;

	public WorkspaceException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	public ErrorCode getErrorCode() {
		return errorCode;
	}

}
