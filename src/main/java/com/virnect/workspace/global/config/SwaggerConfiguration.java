package com.virnect.workspace.global.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.global.error.ErrorResponseMessage;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-30
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Profile({"!staging", "!production"})
@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfiguration {
	private final ObjectMapper objectMapper;

	@Bean
	public Docket docket() throws JsonProcessingException {
		Contact contact = new Contact("이주경", "https://virnect.com", "ljk@virnect.com");

		ApiInfo apiInfo = new ApiInfoBuilder()
			.contact(contact)
			.description("워크스페이스 API")
			.version("v0.0.1")
			.title("VIRNECT Platform Workspace Service API Document.")
			.license("VIRNECT INC All rights reserved.")
			.build();

		List<ResponseMessage> responseMessages = new ArrayList<>();
		for (ErrorCode errorCode : ErrorCode.values()) {
			responseMessages.add(new ResponseMessageBuilder().code(errorCode.getCode())
				.message(objectMapper.writeValueAsString(new ErrorResponseMessage(errorCode)))
				.build());
		}
		responseMessages.add(new ResponseMessageBuilder().code(200).message("success").build());

		return new Docket(DocumentationType.SWAGGER_2)
			.useDefaultResponseMessages(false)
			.globalResponseMessage(RequestMethod.GET, responseMessages)
			.globalResponseMessage(RequestMethod.POST, responseMessages)
			.globalResponseMessage(RequestMethod.PUT, responseMessages)
			.globalResponseMessage(RequestMethod.DELETE, responseMessages)
			.select()
			.apis(RequestHandlerSelectors.basePackage("com.virnect.workspace.api"))
			.paths(PathSelectors.any())
			.build()
			.apiInfo(apiInfo);

	}
}
