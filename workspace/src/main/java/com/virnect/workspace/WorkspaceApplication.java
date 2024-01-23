package com.virnect.workspace;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import com.virnect.workspace.global.common.RedirectProperty;

@EnableConfigurationProperties(RedirectProperty.class)
@SpringBootApplication
@EnableAsync
public class WorkspaceApplication {
	public static void main(String[] args) {
		SpringApplication.run(WorkspaceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
