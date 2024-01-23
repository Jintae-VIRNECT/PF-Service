package com.virnect.license.event.license;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LicenseExpiredEvent {
	private final String pushService;
	private final String pushTargetWorkspaceId;
	private final String pushUserId = "system";
	private final String pushReceiverUserId;
	private final Map<Object, Object> message;
	private final String event = "licenseExpired";

	@Override
	public String toString() {
		return "LicenseExpiredEvent{" +
			"pushService='" + pushService + '\'' +
			", pushTargetWorkspaceId='" + pushTargetWorkspaceId + '\'' +
			", pushUserId='" + pushUserId + '\'' +
			", pushReceiverUserId='" + pushReceiverUserId + '\'' +
			", message=" + message +
			", event='" + event + '\'' +
			'}';
	}
}
