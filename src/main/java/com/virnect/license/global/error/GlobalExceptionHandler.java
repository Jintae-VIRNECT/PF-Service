package com.virnect.license.global.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.AES256Utils;
import com.virnect.license.global.middleware.EncodingRequestResponse;
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
    private final String SECRET_KEY = "$37$15$TceNRIvGL$37$15$TceNRIvGL";
    private final ObjectMapper objectMapper;

    /**
     * 라이선스 서비스 예외 발생
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
     * @param e - 빌링 서비스 예외 발생 정보
     * @return - 암호화된 에러 응답 메시지
     * @throws JsonProcessingException
     */
    @ExceptionHandler(BillingServiceException.class)
    public ResponseEntity<EncodingRequestResponse> billingServiceException(BillingServiceException e) throws JsonProcessingException {
        log.error("[LICENSE_SERVICE - EXCEPTION] - MESSAGE: [{}] , DATA: [{}]", e.getMessage(), e.getError());
        String responseJson = objectMapper.writeValueAsString(new ErrorResponseMessage(e.getError()));
        log.info("[BILLING][ERROR_RESPONSE][JSON][NOT_ENCRYPTED] - {}", responseJson);
        String encryptedResponse = AES256Utils.encrypt(SECRET_KEY, responseJson);
        EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
        encodingRequestResponse.setData(encryptedResponse);
        return ResponseEntity.ok(encodingRequestResponse);
    }

    /**
     * 빌링 서비스 예외 발생 (상품 지급 실패)
     * @param e - 빌링 서비스 예외 발생 정보
     * @return - 암호화된 에러 응답 메시지
     * @throws JsonProcessingException
     */
    @ExceptionHandler(LicenseAllocateDeniedException.class)
    public ResponseEntity<EncodingRequestResponse> loginFailureExceptionHandler(LicenseAllocateDeniedException e) throws JsonProcessingException {
        ErrorResponseMessage errorResponseMessage = new ErrorResponseMessage(e.getError());
        errorResponseMessage.getData().put("userId", e.getUserId());
        errorResponseMessage.getData().put("isAssignable", e.isAssignable());
        String responseJson = objectMapper.writeValueAsString(errorResponseMessage);
        log.info("[BILLING][ERROR_RESPONSE][JSON][NOT_ENCRYPTED] - {}", responseJson);
        String encryptedResponse = AES256Utils.encrypt(SECRET_KEY, responseJson);
        EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
        encodingRequestResponse.setData(encryptedResponse);
        return ResponseEntity.ok(encodingRequestResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseMessage> generalExceptionHandler(Exception e) {
        log.error(e.getMessage());
        return ResponseEntity.ok(new ErrorResponseMessage(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
    }
}
