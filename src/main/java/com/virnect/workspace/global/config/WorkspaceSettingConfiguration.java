package com.virnect.workspace.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.virnect.workspace.application.WorkspaceSettingInterceptor;

/**
 * Project: PF-Workspace
 * DATE: 2021-06-04
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Configuration
public class WorkspaceSettingConfiguration implements WebMvcConfigurer {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(workspaceSettingInterceptor())
			.addPathPatterns("/");//특정 url
	}

	@Bean
	public WorkspaceSettingInterceptor workspaceSettingInterceptor() {
		return new WorkspaceSettingInterceptor();
	}
}
