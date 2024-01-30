package com.virnect.platform.feign.rest.process;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.virnect.platform.feign.rest.process.dto.SubProcessCountResponse;
import com.virnect.platform.workspace.global.common.ApiResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-03-24
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@FeignClient(name = "process-server", fallbackFactory = ProcessRestFallbackFactory.class)
public interface ProcessRestService {
	/**
	 * smic 때 사용했던 api
	 * @param workerUUID
	 * @return
	 */
	@GetMapping("/processes/subProcesses/count/onWorker/{workerUUID}")
	ApiResponse<SubProcessCountResponse> getSubProcessCount(@PathVariable("workerUUID") String workerUUID);

}
