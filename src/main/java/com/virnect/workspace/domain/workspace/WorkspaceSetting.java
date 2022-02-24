package com.virnect.workspace.domain.workspace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-16
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Audited
@Table(name = "workspace_setting")
public class WorkspaceSetting extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "workspace_setting_id")
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "default_logo", nullable = false)
	private String defaultLogo;

	@Column(name = "grey_logo", nullable = true)
	private String greyLogo;

	@Column(name = "white_logo", nullable = true)
	private String whiteLogo;

	@Column(name = "favicon", nullable = false)
	private String favicon;

	@Column(name = "remote_android_splash_logo")
	private String remoteAndroidSplashLogo;

	@Column(name = "remote_android_login_logo")
	private String remoteAndroidLoginLogo;

	@Column(name = "remote_hololens2_common_logo")
	private String remoteHololens2CommonLogo;

	@Column(name = "workspace_id")
	private String workspaceId;

	@Builder(builderClassName = "WorkspaceSettingBuilder", builderMethodName = "workspaceSettingBuilder")
	public WorkspaceSetting(String title, String defaultLogo, String greyLogo, String whiteLogo, String favicon) {
		this.title = title;
		this.defaultLogo = defaultLogo;
		this.greyLogo = greyLogo;
		this.whiteLogo = whiteLogo;
		this.favicon = favicon;
	}

	@Builder(builderClassName = "WorkspaceSettingInitBuilder", builderMethodName = "workspaceSettingInitBuilder")
	public WorkspaceSetting(
		String defaultLogo, String whiteLogo, String favicon, String remoteAndroidSplashLogo,
		String remoteAndroidLoginLogo,
		String remoteHololens2CommonLogo,
		String workspaceId
	) {
		this.title = "VIRNCET";
		this.defaultLogo = defaultLogo;
		this.greyLogo = null;
		this.whiteLogo = whiteLogo;
		this.favicon = favicon;
		this.remoteAndroidSplashLogo = remoteAndroidSplashLogo;
		this.remoteAndroidLoginLogo = remoteAndroidLoginLogo;
		this.remoteHololens2CommonLogo = remoteHololens2CommonLogo;
		this.workspaceId = workspaceId;
	}

}
