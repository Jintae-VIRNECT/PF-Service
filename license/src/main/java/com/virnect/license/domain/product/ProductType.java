package com.virnect.license.domain.product;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.virnect.license.domain.BaseTimeEntity;

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
@Table(name = "product_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductType extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_type_id")
	Long id;

	@Column(name = "name")
	private String name;

	@OneToMany(mappedBy = "productType", fetch = FetchType.LAZY)
	private List<Product> productList = new ArrayList<>();

	public ProductType(String productTypeName) {
		this.name = productTypeName;
	}
}
