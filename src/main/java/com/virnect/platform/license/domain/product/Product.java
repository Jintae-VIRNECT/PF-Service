package com.virnect.platform.license.domain.product;

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

import org.hibernate.envers.Audited;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.platform.license.domain.BaseTimeEntity;

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
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "porduct_id")
	private Long id;

	@Column(name = "billing_product_id")
	private Long billProductId;

	@Column(name = "name")
	private String name;

	@Column(name = "max_storage_size", nullable = false)
	private Long maxStorageSize;

	@Column(name = "max_download_hit", nullable = false)
	private Long maxDownloadHit;

	@Column(name = "max_call_time", nullable = false)
	private Long maxCallTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_type_id")
	private ProductType productType;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ProductStatus displayStatus;

	@Builder
	public Product(
		String name, Long maxStorageSize, Long maxDownloadHit, Long maxCallTime,
		ProductType productType, Long billProductId
	) {
		this.name = name;
		this.maxStorageSize = maxStorageSize;
		this.maxDownloadHit = maxDownloadHit;
		this.maxCallTime = maxCallTime;
		this.productType = productType;
		this.displayStatus = ProductStatus.ACTIVE; // new product info is will not displayed by default
		this.billProductId = billProductId;
	}
}
