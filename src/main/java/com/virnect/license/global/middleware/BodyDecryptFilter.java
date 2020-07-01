package com.virnect.license.global.middleware;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class BodyDecryptFilter implements Filter {
    private final String SECRET_KEY = "$37$15$TceNRIvGL$37$15$TceNRIvGL";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequestBodyDecodingWrapper requestWrapper = new HttpServletRequestBodyDecodingWrapper((HttpServletRequest) request, SECRET_KEY);
        log.info("[BILLING][REQUEST][DECODING_FILTER] - BEGIN.");
        chain.doFilter(requestWrapper, response);
        log.info("[BILLING][REQUEST][DECODING_FILTER] - END.");
    }
}
