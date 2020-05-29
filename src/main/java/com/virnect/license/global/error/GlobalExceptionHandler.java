package com.virnect.license.global.error;

import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.exception.LicenseServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Project: PF-User
 * DATE: 2020-01-22
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: Global Exception Handler
 */

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(LicenseServiceException.class)
    public ResponseEntity<ErrorResponseMessage> licenseServiceException(LicenseServiceException e) {
        log.error("[LICENSE_SERVICE - EXCEPTION] - MESSAGE: [{}] , DATA: [{}]", e.getMessage(), e.getError());
        return ResponseEntity.ok(new ErrorResponseMessage(e.getError()));
    }


    @ExceptionHandler(LicenseAllocateDeniedException.class)
    public ResponseEntity<ErrorResponseMessage> loginFailureExceptionHandler(LicenseAllocateDeniedException e) {
        ErrorResponseMessage errorResponseMessage = new ErrorResponseMessage(e.getError());
        errorResponseMessage.getData().put("userId", e.getUserId());
        errorResponseMessage.getData().put("isAssignable", e.isAssignable());
        return ResponseEntity.ok(errorResponseMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseMessage> generalExceptionHandler(Exception e) {
        e.printStackTrace();
        return ResponseEntity.ok(new ErrorResponseMessage(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
    }
}
