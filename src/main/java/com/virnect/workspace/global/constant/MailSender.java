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
    private String sender;

    MailSender(String sender) {
        this.sender = sender;
    }

}
