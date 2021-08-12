package com.virnect.workspace.dto.rest;

import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-12
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class SeatMemberRegistrationRequest {
    private String masterUserUUID;
    private String workspaceUUID;

    @Override
    public String toString() {
        return "SeatMemberRegistrationRequest{" +
                "masterUserUUID='" + masterUserUUID + '\'' +
                ", workspaceUUID='" + workspaceUUID + '\'' +
                '}';
    }
}
