package com.virnect.license.global.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.virnect.license.global.middleware.BillingRequestBodyDecryptFilter;
import com.virnect.license.global.middleware.BillingResponseBodyEncryptFilter;

@Profile({"staging", "production"})
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer, WebMvcRegistrations {
	private static final List<String> BILLING_API_URLS = Arrays.asList("/licenses/allocate/check", "/licenses/allocate",
		"/licenses/deallocate"
	);

	@Bean
	public FilterRegistrationBean<BillingRequestBodyDecryptFilter> decodingFilter() {
		FilterRegistrationBean<BillingRequestBodyDecryptFilter> requestDecodingFilterBean = new FilterRegistrationBean<>();
		requestDecodingFilterBean.setFilter(new BillingRequestBodyDecryptFilter());
		requestDecodingFilterBean.setUrlPatterns(BILLING_API_URLS);
		return requestDecodingFilterBean;
	}

	@Bean
	public FilterRegistrationBean<BillingResponseBodyEncryptFilter> encodingFilter() {
		FilterRegistrationBean<BillingResponseBodyEncryptFilter> responseEncodingFilterBean = new FilterRegistrationBean<>();
		responseEncodingFilterBean.setFilter(new BillingResponseBodyEncryptFilter());
		responseEncodingFilterBean.setUrlPatterns(BILLING_API_URLS);
		return responseEncodingFilterBean;
	}
}
