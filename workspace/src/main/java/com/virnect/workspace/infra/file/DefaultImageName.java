package com.virnect.workspace.infra.file;

/**
 * Project: PF-Workspace
 * DATE: 2021-09-28
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public enum DefaultImageName {
	WORKSPACE_PROFILE("workspace-profile.png"),
	REMOTE_ANDROID_SPLASH_LOGO("remote-android-splash-logo.png"),
	REMOTE_ANDROID_LOGIN_LOGO("remote-android-login-logo.png"),
	REMOTE_HOLOLENS2_COMMON_LOGO("remote-hololens2-common-logo.png");

	private final String name;

	DefaultImageName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
