package com.virnect.workspace.application.message.dto;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Message
 * DATE: 2020-06-15
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class PushSendRequest {
	@ApiModelProperty(value = "푸시 발송 서비스 명", example = "pf-workspace", required = true, position = 0)
	@NotBlank
	private String service;

	@ApiModelProperty(value = "워크스페이스 식별자", example = "4ff0606102fbe", required = true, position = 1)
	@NotBlank
	private String workspaceId;

	@ApiModelProperty(value = "메세지 발행 유저 식별자", example = "4ff0606102fbe", required = true, position = 2)
	@NotBlank
	private String userId;

	@ApiModelProperty(value = "메세지 수신 대상 식별자", example = "[\"4ff0606102fbe\"]", required = true, position = 3)
	@NotNull
	private List<String> targetUserIds;

	@ApiModelProperty(value = "이벤트 이름", example = "WORKSPACE_EXPELED", required = true, position = 4)
	@NotBlank
	private String event;

	@ApiModelProperty(value = "메세지 내용", example = "{\n" +
		"  \"custom1\": \"string\",\n" +
		"  \"custom2\": \"string\"\n" +
		"}", required = true, position = 5)
	private Map<Object, Object> contents;

	@Override
	public String toString() {
		return "PushSendRequest{" +
			"service='" + service + '\'' +
			", workspaceId='" + workspaceId + '\'' +
			", userId='" + userId + '\'' +
			", targetUserIds=" + targetUserIds +
			", event='" + event + '\'' +
			", contents=" + contents +
			'}';
	}
}
