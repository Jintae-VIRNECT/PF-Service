package com.virnect.platform.workspace.global.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-09
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
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

