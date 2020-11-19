package com.virnect.workspace.global.common;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Project: PF-Workspace
 * DATE: 2020-11-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "redirect")
@Getter
public class RedirectProperty {
    private final String workspaceServer;
    private final String workstationWeb;
    private final String contactWeb;
    private final String accountWeb;
    private final String supportWeb;
    private final String signupWeb;
    private final String membersWeb;

    public RedirectProperty(String workspaceServer, String workstationWeb, String contactWeb, String accountWeb, String supportWeb, String signupWeb, String membersWeb) {
        this.workspaceServer = workspaceServer;
        this.workstationWeb = workstationWeb;
        this.contactWeb = contactWeb;
        this.accountWeb = accountWeb;
        this.supportWeb = supportWeb;
        this.signupWeb = signupWeb;
        this.membersWeb = membersWeb;
    }

    @Override
    public String toString() {
        return "RedirectProperty{" +
                "workspaceServer='" + workspaceServer + '\'' +
                ", workstationWeb='" + workstationWeb + '\'' +
                ", contactWeb='" + contactWeb + '\'' +
                ", accountWeb='" + accountWeb + '\'' +
                ", supportWeb='" + supportWeb + '\'' +
                ", signupWeb='" + signupWeb + '\'' +
                ", membersWeb='" + membersWeb + '\'' +
                '}';
    }
}
