package com.virnect.workspace.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

/**
 * Project: base
 * DATE: 2020-01-07
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: Abstract Time Entity for record datetime column on every table as column, All Entity should be extends this Entity Class
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Audited
public abstract class TimeEntity {
	@Column(name = "created_at", nullable = false)
	@CreatedDate
	private LocalDateTime createdDate;

	@Column(name = "updated_at", nullable = false)
	@LastModifiedDate
	private LocalDateTime updatedDate;
}
