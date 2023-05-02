package com.virnect.workspace.domain.workspace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Entity
@Getter
@Setter
@Table(name = "workspace_user_license")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Audited
public class WorkspaceUserLicense extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "workspace_user_license_id")
	private Long id;

	@Column(name = "license_id", unique = true)
	private Long licenseId;

	@NotAudited
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_user_id")
	private WorkspaceUser workspaceUser;
}
