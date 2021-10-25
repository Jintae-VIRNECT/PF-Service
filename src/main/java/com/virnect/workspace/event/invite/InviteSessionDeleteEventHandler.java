package com.virnect.workspace.event.invite;

import com.virnect.workspace.dao.cache.UserInviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Project: PF-Workspace
 * DATE: 2021-07-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class InviteSessionDeleteEventHandler {
    private final UserInviteRepository userInviteRepository;

    @EventListener(InviteSessionDeleteEvent.class)
    public void inviteSessionDeleteEventListener(InviteSessionDeleteEvent inviteSessionDeleteEvent) {
        log.info("[WORKSPACE INVITE SESSION DELETE EVENT] - [{}]", inviteSessionDeleteEvent.getSessionCode());
        userInviteRepository.deleteById(inviteSessionDeleteEvent.getSessionCode());
    }
}
