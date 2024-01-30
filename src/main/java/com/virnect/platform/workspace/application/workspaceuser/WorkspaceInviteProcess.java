package com.virnect.platform.workspace.application.workspaceuser;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.view.RedirectView;

import lombok.Builder;

import com.virnect.platform.workspace.event.history.HistoryAddEvent;
import com.virnect.platform.workspace.event.invite.InviteSessionDeleteEvent;
import com.virnect.platform.workspace.event.message.MailSendEvent;
import com.virnect.platform.workspace.global.constant.Mail;

/**
 * Project: PF-Workspace
 * DATE: 2021-07-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public class WorkspaceInviteProcess {
	private final ApplicationEventPublisher applicationEventPublisher;
	private final InviteSessionDeleteEvent inviteSessionDeleteEvent;
	private final String redirectUrl;
	private final MailSendEvent mailSendEvent;
	private final HistoryAddEvent historyAddEvent;

	@Builder
	public WorkspaceInviteProcess(
		ApplicationEventPublisher applicationEventPublisher, InviteSessionDeleteEvent inviteSessionDeleteEvent, String redirectUrl,
		MailSendEvent mailSendEvent, HistoryAddEvent historyAddEvent
	) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.inviteSessionDeleteEvent = inviteSessionDeleteEvent;
		this.redirectUrl = redirectUrl;
		this.mailSendEvent = mailSendEvent;
		this.historyAddEvent = historyAddEvent;
	}

	public RedirectView process() {

		//1. 실패/성공 메일 전송
		applicationEventPublisher.publishEvent(mailSendEvent);

		//2. 초대 세션 삭제
		applicationEventPublisher.publishEvent(inviteSessionDeleteEvent);

		//3. 초대 성공 히스토리 저장
		if (mailSendEvent.getMailType().equals(Mail.WORKSPACE_INVITE_ACCEPT)) {
			applicationEventPublisher.publishEvent(historyAddEvent);
		}

		//4. redirectView 리턴
		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(redirectUrl);
		redirectView.setContentType("application/json");
		return redirectView;
	}
}
