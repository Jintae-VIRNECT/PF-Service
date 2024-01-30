package com.virnect.platform.feign.rest.message;

import org.springframework.stereotype.Service;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.feign.rest.message.dto.MailRequest;
import com.virnect.platform.global.common.ApiResponse;
import com.virnect.platform.license.dto.rest.message.PushRequest;
import com.virnect.platform.license.dto.rest.message.PushResponse;

@Slf4j
@Service
public class MessageRestServiceFallbackFactory implements FallbackFactory<MessageRestService> {
	@Override
	public MessageRestService create(Throwable cause) {
		log.error("[MESSAGE_PUSH_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
		log.error(cause.getMessage(), cause);
		return new MessageRestService() {
			@Override
			public ApiResponse<PushResponse> sendPush(PushRequest pushSendRequest) {
				log.error("[MESSAGE_PUSH_REST_SERVICE][REQUEST] - {}");
				ApiResponse<PushResponse> pushResponseApiResponse = new ApiResponse<>();
				pushResponseApiResponse.setCode(500);
				pushResponseApiResponse.setData(null);
				pushResponseApiResponse.setMessage("push server rest error");
				return pushResponseApiResponse;
			}

			@Override
			public void sendMail(MailRequest mailSendRequest) {

			}
		};
	}
}
