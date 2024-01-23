package com.virnect.workspace.global.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-27
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Profile("!local")
@Configuration
@EnableDiscoveryClient
public class EurekaConfiguration {
}
