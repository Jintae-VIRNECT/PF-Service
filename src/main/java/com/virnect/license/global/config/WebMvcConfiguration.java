package com.virnect.license.global.config;

import com.virnect.license.global.middleware.BodyDecryptFilter;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer, WebMvcRegistrations {
    @Bean
    public FilterRegistrationBean<BodyDecryptFilter> decodingFilter() {
        FilterRegistrationBean<BodyDecryptFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new BodyDecryptFilter());
        bean.setUrlPatterns(Arrays.asList("/licenses/allocate/check", "/licenses/allocate", "/licenses/deallocate"));
        return bean;
    }
}
