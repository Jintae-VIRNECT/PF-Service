package com.virnect.license.domain.applicense;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;

import com.virnect.license.domain.BaseTimeEntity;

@Entity
@Getter
@Setter
@Table(name = "app_license")
public class AppLicense extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "app_license_id")
	private Long id;

	@Column(name = "serial_key")
	private String serialKey;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private AppLicenseStatus status;

	@Column(name = "started_at")
	private LocalDateTime startDate;

	@Column(name = "expired_at")
	private LocalDateTime expiredDate;

	@Column(name = "hit")
	private Long hit;

	@Override
	public String toString() {
		return "AppLicense{" +
			"id=" + id +
			", serialKey='" + serialKey + '\'' +
			", status=" + status +
			", startDate=" + startDate +
			", expiredDate=" + expiredDate +
			", hit=" + hit +
			'}';
	}

	public void increaseHit() {
		this.hit += 1;
	}
}
