package com.virnect.platform.global.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Project: ServiceServer
 * DATE: 2019-09-18
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: Response Message Builder
 */

@Getter
@Setter
@NoArgsConstructor
public class ResponseMessage {
	Map<String, Object> data = new ConcurrentHashMap<>();
	int code = 200;
	String message = "complete";

	public ResponseMessage(Map<String, Object> data) {
		this.data = data;
	}

	public ResponseMessage addParam(String key, Object object) {
		this.data.put(key, object);
		return this;
	}
}
