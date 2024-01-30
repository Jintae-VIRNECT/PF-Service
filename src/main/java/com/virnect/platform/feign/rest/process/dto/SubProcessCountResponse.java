package com.virnect.platform.feign.rest.process.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-03-24
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class SubProcessCountResponse {
	private String workerUUID;
	private Integer countProgressing;
	private Integer countAssigned;
}
