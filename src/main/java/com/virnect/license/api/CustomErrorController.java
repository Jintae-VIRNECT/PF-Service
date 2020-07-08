package com.virnect.license.api;

import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.global.error.ErrorResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public ResponseEntity<ErrorResponseMessage> error(HttpServletRequest request) {
        Map<String, String> headers = Collections.list(request.getHeaderNames())
                .stream()
                .collect(Collectors.toMap(h -> h, request::getHeader));
        headers.keySet().forEach(key -> log.info("[CUSTOM_ERROR_CONTROLLER][HEADER] - [{}] -> [{}]", key, headers.get(key)));
        return ResponseEntity.ok(new ErrorResponseMessage(ErrorCode.ERR_INVALID_REQUEST_PARAMETER));
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
