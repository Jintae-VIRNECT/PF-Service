package com.virnect.license.event.license;

import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.rest.message.MessageRestService;
import com.virnect.license.dto.rest.message.PushRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseExpiredEventListener {
	private final MessageRestService messageRestService;

	@TransactionalEventListener()
	public void sendLicenseExpiredEventMessage(LicenseExpiredEvent licenseExpiredEvent) {
		PushRequest pushRequest = new PushRequest();
		pushRequest.setService(licenseExpiredEvent.getPushService());
		pushRequest.setWorkspaceId(licenseExpiredEvent.getPushTargetWorkspaceId());
		pushRequest.setUserId(licenseExpiredEvent.getPushUserId());
		pushRequest.setEvent(licenseExpiredEvent.getEvent());
		pushRequest.setContents(licenseExpiredEvent.getMessage());
		if (StringUtils.hasText(licenseExpiredEvent.getPushReceiverUserId())) {
			pushRequest.setTargetUserIds(Collections.singletonList(licenseExpiredEvent.getPushReceiverUserId()));
			messageRestService.sendPush(pushRequest);
			log.info("[LICENSE_EXPIRED_EVENT_MESSAGE][SEND_PUSH] - {}", pushRequest.toString());
		} else {
			log.info("[LICENSE_EXPIRED_EVENT_MESSAGE][TARGET_USER_NOT_EXIST][PUSH_SKIP] - {}", pushRequest.toString());
		}
	}
}
