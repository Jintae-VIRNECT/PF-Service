package com.virnect.workspace.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignFormatterRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;

import feign.Retryer;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-17
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@Configuration
@EnableFeignClients(basePackages = "com.virnect.workspace.application")
@RequiredArgsConstructor
public class FeignConfiguration {
	// Feign retry set
	@Bean
	public Retryer retryer() {
		return new Retryer.Default(1000, 2000, 3);
	}

	/**
	 * RequestParam 에서 LocalDate, LocalDateTime, LocalTime 을 사용을 할 때 ISO formatter 로 보내기 위한 설정
	 *
	 * @see org.springframework.web.bind.annotation.RequestParam
	 */
	@Bean
	public FeignFormatterRegistrar localDateFeignFormatterRegister() {
		return registry -> {
			DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
			registrar.setUseIsoFormat(true);
			registrar.registerFormatters(registry);
		};
	}
}
