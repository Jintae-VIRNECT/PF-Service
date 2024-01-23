package com.virnect.workspace.application.message.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ApiModel
@Getter
@RequiredArgsConstructor
public class PushResponse {
	@ApiModelProperty(value = "메시지 수신 서비스 명", example = "workspace")
	private final String service;
	@ApiModelProperty(value = "메시지 이벤트", position = 1, example = "something event")
	private final String event;
	@ApiModelProperty(value = "메시지 수신 워크스페이스 식별자", position = 2, example = "4d6eab0860969a50acbfa4599fbb5ae8")
	private final String workspaceId;
	@ApiModelProperty(value = "메시지 발신 완료 여부", position = 3, example = "true")
	private final boolean isPublished;
	@ApiModelProperty(value = "메시지 발신 일자", position = 4, example = "2020-07-17T15:44:20")
	private final LocalDateTime publishDate;

	@Override
	public String toString() {
		return "PushResponse{" +
			"service='" + service + '\'' +
			", event='" + event + '\'' +
			", workspaceId='" + workspaceId + '\'' +
			", isPublished=" + isPublished +
			'}';
	}
}
