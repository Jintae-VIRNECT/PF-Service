package com.virnect.license.infra.billing;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConstructorBinding
@ConfigurationProperties(prefix = "infra.billing")
@Validated
public class BillingServiceProperty {
	@NotBlank(message = "외부 빌링 시스템 api 주소 정보는 반드시 설정되어있어야 합니다.")
	private final String api;

	public BillingServiceProperty(String api) {
		this.api = api;
		log.info("EXTERNAL BILLING SERVICE API: [{}]", api);
	}

	public String getApi() {
		return api;
	}
}
