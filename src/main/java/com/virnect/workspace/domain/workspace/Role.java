package com.virnect.workspace.domain.workspace;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-09
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public enum Role {
    MASTER,
    MANAGER,
    MEMBER,
    GUEST;

    /*public static boolean anyMatchMasterManagerMember(Role requestRole) {
        return requestRole == MASTER || requestRole == MANAGER || requestRole == MEMBER;
    }

    public static boolean anyMatchMasterManager(Role requestRole) {
        return requestRole == MASTER || requestRole == MANAGER;
    }*/

}
