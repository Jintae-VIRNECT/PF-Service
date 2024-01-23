package com.virnect.license.global.middleware;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BillingRequestBodyDecryptFilter implements Filter {
	private final String SECRET_KEY = "$37$15$TceNRIvGL$37$15$TceNRIvGL";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws
		IOException,
		ServletException {
		RequestBodyDecodingWrapper requestWrapper = new RequestBodyDecodingWrapper(
			(HttpServletRequest)request,
			SECRET_KEY
		);
		log.info("[BILLING][REQUEST][DECODING_FILTER] - BEGIN.");
		chain.doFilter(requestWrapper, response);
		log.info("[BILLING][REQUEST][DECODING_FILTER] - END.");
	}
}
