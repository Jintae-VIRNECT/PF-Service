package com.virnect.license.dto.rest.message;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class PushRequest {
	@ApiModelProperty(value = "Push Service Name", example = "remote", required = true)
	@NotBlank
	private String service;

	@ApiModelProperty(value = "Workspace Identifier", example = "40f9bbee9d85dca7a34a0dd205aae718", required = true, position = 1)
	@NotBlank
	private String workspaceId;

	@ApiModelProperty(value = "Sender User Unique Identifier", example = "410df50ca6e32db0b6acba09bcb457ff", required = true, position = 2)
	@NotBlank
	private String userId;

	@ApiModelProperty(
		value = "Receiver Target User Unique Identifier",
		example = "[\n" +
			" \"4705cf50e6d02c59b0eef9591666e2a3\",\n" +
			" \"473b12854daa6afeb9e505551d1b2743\",\n" +
			"\n" +
			"]",
		required = true,
		position = 3)
	@NotNull
	private List<String> targetUserIds;

	@ApiModelProperty(value = "Push Event Name", example = "invite", required = true, position = 4)
	@NotBlank
	private String event;

	@ApiModelProperty(value = "Push Contents", example = "{\n" +
		"  \"custom1\": \"string\",\n" +
		"  \"custom2\": \"string\"\n" +
		"}", required = true, position = 5)
	private Map<Object, Object> contents;
}
