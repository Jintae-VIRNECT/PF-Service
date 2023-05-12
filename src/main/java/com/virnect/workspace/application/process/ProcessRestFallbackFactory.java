package com.virnect.workspace.application.process;

import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.application.process.dto.SubProcessCountResponse;
import com.virnect.workspace.global.common.ApiResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-29
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Component
public class ProcessRestFallbackFactory implements FallbackFactory<ProcessRestService> {
	@Override
	public ProcessRestService create(Throwable cause) {
		log.error(cause.getMessage(), cause);
		return workerUUID -> {
			SubProcessCountResponse subProcessCountResponse = new SubProcessCountResponse();
			return new ApiResponse<>(subProcessCountResponse);
		};
	}
}
