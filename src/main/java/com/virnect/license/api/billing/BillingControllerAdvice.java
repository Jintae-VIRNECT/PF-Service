package com.virnect.license.api.billing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.AES256Utils;
import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.global.middleware.EncodingRequestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = BillingController.class)
@RequiredArgsConstructor
public class BillingControllerAdvice implements ResponseBodyAdvice<Object> {
    private final String SECRET_KEY = "$37$15$TceNRIvGL$37$15$TceNRIvGL";
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            String responseJson = objectMapper.writeValueAsString(body);
            log.info(responseJson);
            String encodedString = AES256Utils.encrypt(SECRET_KEY, responseJson);
            EncodingRequestResponse encodingRequestResponse = new EncodingRequestResponse();
            encodingRequestResponse.setData(encodedString);
            response.setStatusCode(HttpStatus.OK);
            return encodingRequestResponse;
        } catch (JsonProcessingException e) {
            log.error("[BILLING_CONTROLLER_RESPONSE_ADVICE] - {}", e.getMessage());
            throw new BillingServiceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
        }
    }
}
