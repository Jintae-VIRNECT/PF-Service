package com.virnect.license.application.rest.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.virnect.license.dto.rest.message.PushRequest;
import com.virnect.license.dto.rest.message.PushResponse;
import com.virnect.license.global.common.ApiResponse;

@FeignClient(name = "message-server", fallbackFactory = MessageRestServiceFallbackFactory.class)
public interface MessageRestService {
	@PostMapping("/messages/push")
	ApiResponse<PushResponse> sendPush(@RequestBody PushRequest pushSendRequest);
}
