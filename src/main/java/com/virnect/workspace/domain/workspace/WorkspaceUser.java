package com.virnect.workspace.domain.workspace;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;
import com.virnect.workspace.domain.group.GroupUser;

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
@Table(name = "workspace_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Audited
public class WorkspaceUser extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "workspace_user_id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private String userId; // user uuid

	@ManyToOne(targetEntity = Workspace.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;

	@NotAudited
	@OneToMany(mappedBy = "workspaceUser")
	private List<GroupUser> groupUserList = new ArrayList<>();

	@Builder
	public WorkspaceUser(String userId, Workspace workspace) {
		this.userId = userId;
		this.workspace = workspace;
	}
}
