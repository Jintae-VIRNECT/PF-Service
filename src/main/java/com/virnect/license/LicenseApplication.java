package com.virnect.license;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

import com.virnect.license.infra.billing.BillingServiceProperty;

@Slf4j
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


	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		String msg = "\n\n----------------------------------------------------\n" + "\n"
			+ "   Platform License Server is ready!\n"
			+ "   ---------------------------\n" + "\n"
			+ "   * Server Host: [" + System.getenv("eureka.instance.ip-address") + "]\n" + "\n"
			+ "   * VIRNECT_ENV: [" + System.getenv("VIRNECT_ENV") + "]\n" + "\n"
			+ "   * Config Server Url: [" + System.getenv("CONFIG_SERVER") + "]\n" + "\n"
			+ "----------------------------------------------------\n";
		log.info(msg);
	}
}
