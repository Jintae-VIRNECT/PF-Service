package com.virnect.workspace.application.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.virnect.workspace.application.message.dto.MailRequest;
import com.virnect.workspace.application.message.dto.PushResponse;
import com.virnect.workspace.application.message.dto.PushSendRequest;
import com.virnect.workspace.global.common.ApiResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@FeignClient(name = "message-server", fallbackFactory = MessageRestFallbackFactory.class)
public interface MessageRestService {
	@PostMapping(value = "/messages/mail")
	void sendMail(@RequestBody MailRequest mailSendRequest);

	@PostMapping(value = "/messages/push")
	ApiResponse<PushResponse> sendPush(@RequestBody PushSendRequest pushSendRequest);
}
