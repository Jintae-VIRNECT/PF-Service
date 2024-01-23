package com.virnect.workspace.domain.group;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

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
@Table(name = "group_permission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupPermission extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "group_permission_id")
	private Long id;

	@Column(name = "permission", nullable = false)
	private String permission;

	@Lob
	@Column(name = "description")
	private String description;

	@Builder
	public GroupPermission(String permission) {
		this.permission = permission;
	}
}
