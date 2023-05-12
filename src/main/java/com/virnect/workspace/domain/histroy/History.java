package com.virnect.workspace.domain.histroy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.workspace.domain.TimeEntity;
import com.virnect.workspace.domain.workspace.Workspace;

/**
 * Project: PF-Workspace
 * DATE: 2020-05-12
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Entity
@Getter
@Setter
@Table(name = "history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class History extends TimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "history_id")
	private Long id;

	@ManyToOne(targetEntity = Workspace.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "message", nullable = false)
	private String message;

	@Builder
	public History(Workspace workspace, String userId, String message) {
		this.workspace = workspace;
		this.userId = userId;
		this.message = message;
	}
}
