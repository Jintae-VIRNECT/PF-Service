package com.virnect.license.api.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virnect.license.exception.LicenseAllocateDeniedException;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.AES256Utils;
import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.global.error.ErrorResponseMessage;
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
@ControllerAdvice(assignableTypes = BillingController.class)
@RequiredArgsConstructor
public class BillingExceptionHandler {
    private final String SECRET_KEY = "$37$15$TceNRIvGL$37$15$TceNRIvGL";
    private final ObjectMapper objectMapper;

    @ExceptionHandler(LicenseServiceException.class)
    public ResponseEntity<EncodingRequestResponse> licenseServiceException(LicenseServiceException e) throws JsonProcessingException {
        log.error("[LICENSE_SERVICE - EXCEPTION] - MESSAGE: [{}] , DATA: [{}]", e.getMessage(), e.getError());
        String responseJson = objectMapper.writeValueAsString(new ErrorResponseMessage(e.getError()));
        log.info("[BILLING][ERROR_RESPONSE][JSON][NOT_ENCRYPTED] - {}", responseJson);
        String encryptedResponse = AES256Utils.encrypt(SECRET_KEY, responseJson);
        EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
        encodingRequestResponse.setData(encryptedResponse);
        return ResponseEntity.ok(encodingRequestResponse);
    }


    @ExceptionHandler(LicenseAllocateDeniedException.class)
    public ResponseEntity<EncodingRequestResponse> loginFailureExceptionHandler(LicenseAllocateDeniedException e) throws JsonProcessingException {
        ErrorResponseMessage errorResponseMessage = new ErrorResponseMessage(e.getError());
        errorResponseMessage.getData().put("userId", e.getUserId());
        errorResponseMessage.getData().put("isAssignable", e.isAssignable());
        String responseJson = objectMapper.writeValueAsString(new ErrorResponseMessage(e.getError()));
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
