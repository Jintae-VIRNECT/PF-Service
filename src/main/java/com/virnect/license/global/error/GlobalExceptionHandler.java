package com.virnect.license.global.error;

import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.exception.LicenseServiceException;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /**
     * 라이선스 서비스 예외 발생
     *
     * @param e - 라이선스 서비스 예외 발생 정보
     * @return - 에러 응답 메시지
     */
    @ExceptionHandler(LicenseServiceException.class)
    public ResponseEntity<ErrorResponseMessage> licenseServiceException(LicenseServiceException e) {
        log.error("[LICENSE_SERVICE - EXCEPTION] - MESSAGE: [{}] , DATA: [{}]", e.getMessage(), e.getError());
        return ResponseEntity.ok(new ErrorResponseMessage(e.getError()));
    }

    /**
     * 빌링 서비스 예외 발생
     *
     * @param e - 빌링 서비스 예외 발생 정보
     * @return - 암호화된 에러 응답 메시지
     */
    @ExceptionHandler(BillingServiceException.class)
    public ResponseEntity<ErrorResponseMessage> billingServiceException(BillingServiceException e) {
        log.error("[LICENSE_SERVICE - BILLING SERVICE - EXCEPTION] - MESSAGE: [{}] , DATA: [{}]", e.getMessage(), e.getError());
        return ResponseEntity.ok(new ErrorResponseMessage(e.getError()));
    }

    /**
     * 빌링 서비스 예외 발생 (상품 지급 실패)
     *
     * @param e - 빌링 서비스 예외 발생 정보
     * @return - 암호화된 에러 응답 메시지
     */
    @ExceptionHandler(LicenseAllocateDeniedException.class)
    public ResponseEntity<ErrorResponseMessage> loginFailureExceptionHandler(LicenseAllocateDeniedException e) {
        log.error("[LICENSE_SERVICE - BILLING SERVICE - EXCEPTION] - MESSAGE: [{}] , DATA: [{}]", e.getMessage(), e.getError());
        ErrorResponseMessage errorResponseMessage = new ErrorResponseMessage(e.getError());
        errorResponseMessage.getData().put("userId", e.getUserId());
        errorResponseMessage.getData().put("isAssignable", e.isAssignable());
        errorResponseMessage.getData().put("rejectInfo", e.getDetails());
        return ResponseEntity.ok(errorResponseMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseMessage> generalExceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.ok(new ErrorResponseMessage(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
    }
}
