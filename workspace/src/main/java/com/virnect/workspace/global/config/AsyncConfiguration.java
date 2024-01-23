package com.virnect.workspace.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-Auth
 * @email practice1356@gmail.com
 * @description Async process configuration
 * @since 2020.04.03
 */

@Configuration
@EnableAsync
public class AsyncConfiguration {

	@Bean
	public TaskExecutor asyncTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize(10);
		taskExecutor.setThreadNamePrefix("async-task-exec-");
		return taskExecutor;
	}
}
