package com.virnect.workspace.application.license.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Project        : PF-Workspace
 * DATE           : 2023-04-04
 * AUTHOR         : VIRNECT-JINTAE (Jintae Kim)
 * EMAIL          : jtkim@virnect.com
 * DESCRIPTION    :
 * ===========================================================
 * DATE            AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2023-04-04      VIRNECT-JINTAE          최초 생성
 */
@Getter
@NoArgsConstructor
public class UserLicenseInfoResponse {
	private List<UserLicenseInfo> userLicenseInfos;
	private String workspaceId;
}
