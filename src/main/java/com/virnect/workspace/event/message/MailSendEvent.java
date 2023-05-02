package com.virnect.workspace.event.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.thymeleaf.context.Context;

import lombok.Getter;

import com.virnect.workspace.global.constant.Mail;

/**
 * Project: PF-Workspace
 * DATE: 2021-07-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
public class MailSendEvent {
	private final Context context;
	private final Mail mailType;
	private final Locale locale;
	private final List<String> receiverList;

	public MailSendEvent(Context context, Mail mailType, Locale locale, List<String> receiverList) {
		this.context = context;
		this.mailType = mailType;
		this.locale = locale;
		this.receiverList = receiverList;
	}

	public MailSendEvent(Context context, Mail mailType, Locale locale, String receiver) {
		this.context = context;
		this.mailType = mailType;
		this.locale = locale;
		List<String> receivers = new ArrayList<>();
		receivers.add(receiver);
		this.receiverList = receivers;
	}

	@Override
	public String toString() {
		return "MailSendEvent{" +
			//"context=" + context +
			"mailType=" + mailType +
			", locale=" + locale +
			", receiverList=" + receiverList +
			'}';
	}
}
