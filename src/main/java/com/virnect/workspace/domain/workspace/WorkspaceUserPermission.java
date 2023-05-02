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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-08
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@Entity
@Getter
@Setter
@Table(name = "workspace_user_permission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Audited
public class WorkspaceUserPermission extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "workspace_user_permission_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_role_id")
	private WorkspaceRole workspaceRole;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_permission_id")
	private WorkspacePermission workspacePermission;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_user")
	private WorkspaceUser workspaceUser;

	@Builder
	public WorkspaceUserPermission(WorkspaceRole workspaceRole, WorkspacePermission workspacePermission, WorkspaceUser workspaceUser) {
		this.workspaceRole = workspaceRole;
		this.workspacePermission = workspacePermission;
		this.workspaceUser = workspaceUser;
	}

}
