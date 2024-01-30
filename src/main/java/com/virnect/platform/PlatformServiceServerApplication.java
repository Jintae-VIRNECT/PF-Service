package com.virnect.platform;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.virnect.platform.workspace.global.common.RedirectProperty;

@EnableConfigurationProperties(RedirectProperty.class)
@SpringBootApplication
@EnableAsync
public class PlatformServiceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlatformServiceServerApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
