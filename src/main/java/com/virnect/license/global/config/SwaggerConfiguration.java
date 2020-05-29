package com.virnect.license.global.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.global.error.ErrorResponseMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMethod;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Project: PF-User
 * DATE: 2020-01-30
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@Profile({"develop", "local"})
@Configuration
@EnableSwagger2
@RequiredArgsConstructor
public class SwaggerConfiguration {
    private final ObjectMapper objectMapper;

    @Bean
    public Docket docket() throws JsonProcessingException {
        Contact contact = new Contact("장정현", "https://virnect.com", "sky456139@vinrect.com");

        ApiInfo apiInfo = new ApiInfoBuilder()
                .contact(contact)
                .description("라이선스 서버 API 정보 입니다.")
                .version("v0.0.1")
                .title("VIRNECT Platform License Service API Document.")
                .license("VIRNECT INC All rights reserved.")
                .build();


        List<ResponseMessage> responseMessages = new ArrayList<>();
        for (ErrorCode errorCode : ErrorCode.values()) {
            responseMessages.add(new ResponseMessageBuilder().code(errorCode.getCode()).message(objectMapper.writeValueAsString(new ErrorResponseMessage(errorCode))).build());
        }
        responseMessages.add(new ResponseMessageBuilder().code(200).message("success").build());

        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .globalResponseMessage(RequestMethod.GET, responseMessages)
                .globalResponseMessage(RequestMethod.POST, responseMessages)
                .globalResponseMessage(RequestMethod.PUT, responseMessages)
                .globalResponseMessage(RequestMethod.DELETE, responseMessages)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.virnect.license.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo);
    }
}
