package com.virnect.workspace.domain.group;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;
import com.virnect.workspace.domain.workspace.WorkspaceUser;

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
@Table(name = "group_User")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupUser extends TimeEntity {
	@OneToMany(mappedBy = "groupUser")
	List<GroupUserPermission> permissions = new ArrayList<>();
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "group_user_id")
	private Long id;
	@Enumerated(EnumType.STRING)
	@Column(name = "favorite")
	private Favorite favorite;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_user_id")
	private WorkspaceUser workspaceUser;

	@Builder
	public GroupUser(Favorite favorite, Group group, WorkspaceUser workspaceUser) {
		this.favorite = favorite;
		this.group = group;
		this.workspaceUser = workspaceUser;
	}
}
