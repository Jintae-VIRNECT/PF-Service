package com.virnect.platform.license.infra.mail;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-Auth
 * @email practice1356@gmail.com
 * @description console email service for development
 * @since 2020.03.25
 */
@Slf4j
@Profile({"test", "local"})
@Component
public class ConsoleEmailService implements EmailService {
	@Override
	public void sendEmail(EmailMessage emailMessage) {
		log.info("sent email: {}", emailMessage.getMessage());
	}
}
