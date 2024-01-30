package com.virnect.platform.feign.rest.message.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-28
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MailRequest {
	@ApiModelProperty(value = "받는 사람", example = "[\"ljk@virnect.com\"]")
	@NotNull
	private List<String> receivers;

	@ApiModelProperty(value = "보내는 사람", example = "no-reply@virnect.com")
	@NotBlank
	private String sender;

	@ApiModelProperty(value = "내용", example = "<html></html>")
	@NotBlank
	private String html;

	@ApiModelProperty(value = "제목", example = "제목")
	@NotBlank
	private String subject;
}
