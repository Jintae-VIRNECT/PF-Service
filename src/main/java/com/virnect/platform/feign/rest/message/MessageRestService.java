package com.virnect.platform.feign.rest.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.virnect.platform.feign.rest.message.dto.MailRequest;
import com.virnect.platform.global.common.ApiResponse;
import com.virnect.platform.license.dto.rest.message.PushRequest;
import com.virnect.platform.license.dto.rest.message.PushResponse;

@FeignClient(name = "message-server", fallbackFactory = MessageRestServiceFallbackFactory.class)
public interface MessageRestService {
	@PostMapping("/messages/push")
	ApiResponse<PushResponse> sendPush(@RequestBody PushRequest pushSendRequest);

	@PostMapping(value = "/messages/mail")
	void sendMail(@RequestBody MailRequest mailSendRequest);

}
