package com.virnect.license.application.rest.message;

import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.message.PushResponse;
import com.virnect.license.global.common.ApiResponse;

@Slf4j
@Service
public class MessageRestServiceFallbackFactory implements FallbackFactory<MessageRestService> {
	@Override
	public MessageRestService create(Throwable cause) {
		log.error("[MESSAGE_PUSH_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
		log.error(cause.getMessage(), cause);
		return message -> {
			log.error("[MESSAGE_PUSH_REST_SERVICE][REQUEST] - {}", message.toString());
			ApiResponse<PushResponse> pushResponseApiResponse = new ApiResponse<>();
			pushResponseApiResponse.setCode(500);
			pushResponseApiResponse.setData(null);
			pushResponseApiResponse.setMessage("push server rest error");
			return pushResponseApiResponse;
		};
	}
}
