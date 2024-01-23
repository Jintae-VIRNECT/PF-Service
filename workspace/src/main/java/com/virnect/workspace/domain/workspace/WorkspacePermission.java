package com.virnect.workspace.domain.workspace;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;
import com.virnect.workspace.global.constant.Permission;

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
@Table(name = "workspace_permission")
@Audited
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspacePermission extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "workspace_permission_id")
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "permission", nullable = false)
	private Permission permission;

	@Lob
	@Column(name = "description")
	private String description;
}
