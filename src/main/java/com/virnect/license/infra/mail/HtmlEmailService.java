package com.virnect.license.infra.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-Auth
 * @email practice1356@gmail.com
 * @description Html Email Message Send Service
 * @since 2020.03.25
 */

@Slf4j
@Profile(value = "!local")
@Component
@RequiredArgsConstructor
public class HtmlEmailService implements EmailService {
    private final JavaMailSender javaMailSender;


    @Override
    @Async("threadPoolTaskExecutor")
    public void sendEmail(EmailMessage emailMessage) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            mimeMessageHelper.setFrom("no-reply@virnect.com");
            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("failed to send email", e);
            throw new RuntimeException(e);
        }
    }
}
