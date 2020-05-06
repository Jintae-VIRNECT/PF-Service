package com.virnect.workspace.application;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-29
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Component
public class MessageRestFallbackFactory implements FallbackFactory<MessageRestService> {
    @Override
    public MessageRestService create(Throwable cause) {
        return null;
    }
}
