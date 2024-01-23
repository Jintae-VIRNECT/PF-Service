package com.virnect.workspace.global.constant;

import lombok.Getter;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-28
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public enum MailSender {
	MASTER("no-reply@virnect.com");

	@Getter
	private final String value;

	MailSender(String value) {
		this.value = value;
	}

}
