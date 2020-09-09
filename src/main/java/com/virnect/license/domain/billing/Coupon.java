package com.virnect.license.domain.billing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.license.domain.BaseTimeEntity;
import com.virnect.license.domain.licenseplan.LicensePlan;

@Getter
@Setter
@Entity
@Table(name = "coupon")
@NoArgsConstructor
public class Coupon extends BaseTimeEntity {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "coupon_id")
	private Long id;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "serial")
	private String serialKey;

	@Column(name = "name")
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "license_plan_id")
	private LicensePlan licensePlan;

	@Builder
	public Coupon(String userId, String serialKey, String name, LicensePlan licensePlan) {
		this.userId = userId;
		this.serialKey = serialKey;
		this.name = name;
		this.licensePlan = licensePlan;
	}
}
