package com.virnect.license.domain.product;

import java.util.Set;

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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.license.domain.BaseTimeEntity;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseType;
import com.virnect.license.domain.licenseplan.LicensePlan;

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
@Table(name = "license_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicenseProduct extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "license_product_id")
	private Long id;

	@Column(name = "quantity")
	private Integer quantity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "license_plan_id")
	private LicensePlan licensePlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "license_type_id")
	private LicenseType licenseType;

	@OneToMany(mappedBy = "licenseProduct", fetch = FetchType.LAZY)
	private Set<License> licenseList;

	@Builder
	public LicenseProduct(Integer quantity, LicensePlan licensePlan, Product product, LicenseType licenseType) {
		this.quantity = quantity;
		this.licensePlan = licensePlan;
		this.product = product;
		this.licenseType = licenseType;
	}
}
