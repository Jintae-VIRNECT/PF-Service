package com.virnect.workspace.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.exception.WorkspaceException;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-09
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// 기타 모든 예외 에러
	@ExceptionHandler(Exception.class)
	public ResponseEntity exception(Exception e) {
		log.error(e.getMessage());
		e.printStackTrace();
		return new ResponseEntity<>(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(WorkspaceException.class)
	protected ResponseEntity<ErrorResponseMessage> handleWorkspaceException(final WorkspaceException e) {
		log.error("handleWorkspaceException", e);
		return ResponseEntity.ok(new ErrorResponseMessage(e.getErrorCode()));
	}
}
