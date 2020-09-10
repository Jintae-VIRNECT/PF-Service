package com.virnect.license;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.virnect.license.infra.billing.BillingServiceProperty;

@SpringBootApplication
@EnableConfigurationProperties({BillingServiceProperty.class})
public class LicenseApplication {

	public static void main(String[] args) {
		SpringApplication.run(LicenseApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
