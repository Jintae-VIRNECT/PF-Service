package com.virnect.license.domain.license;

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
import javax.persistence.Table;

import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.license.domain.BaseTimeEntity;
import com.virnect.license.domain.product.LicenseProduct;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Entity
@Getter
@Setter
@Audited
@Where(clause = "license_status !='TERMINATE'")
@Table(name = "license")
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class License extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "license_id")
	private Long id;

	@Column(name = "serial_key")
	private String serialKey;

	@Column(name = "user_id")
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "license_status")
	private LicenseStatus status = LicenseStatus.UNUSE;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "license_product_id")
	private LicenseProduct licenseProduct;

	@Builder
	public License(String serialKey, LicenseStatus status, LicenseProduct licenseProduct) {
		this.serialKey = serialKey;
		this.status = status;
		this.licenseProduct = licenseProduct;
	}

	@Override
	public String toString() {
		return "License{" +
			"id=" + id +
			", serialKey='" + serialKey + '\'' +
			", userId='" + userId + '\'' +
			", status=" + status +
			'}';
	}
}
