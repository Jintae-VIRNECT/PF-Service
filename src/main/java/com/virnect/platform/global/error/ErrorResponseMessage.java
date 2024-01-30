package com.virnect.platform.global.error;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project: ServiceServer
 * DATE: 2020-01-09
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: Error Response Message Object
 */

@Getter
@NoArgsConstructor
public class ErrorResponseMessage {
	private int code;
	private String service;
	private String message;
	private Map<String, Object> data;

	public ErrorResponseMessage(final ErrorCode error) {
		this.code = error.getCode();
		this.message = error.getMessage();
		data = new HashMap<>();
		this.service = "license";
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
