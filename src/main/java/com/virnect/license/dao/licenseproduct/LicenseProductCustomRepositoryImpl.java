package com.virnect.license.dao.licenseproduct;

import static com.virnect.license.domain.license.QLicense.*;
import static com.virnect.license.domain.product.QLicenseProduct.*;
import static com.virnect.license.domain.product.QProduct.*;
import static com.virnect.license.domain.product.QProductType.*;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import com.virnect.license.domain.billing.ProductTypeId;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;

@RequiredArgsConstructor
public class LicenseProductCustomRepositoryImpl implements LicenseProductCustomRepository {
	private final JPAQueryFactory query;

	@Override
	public List<LicenseProduct> findAllProductLicenseInfoByLicensePlan(
		LicensePlan licensePlan
	) {
		return query.selectFrom(licenseProduct)
			.distinct()
			.innerJoin(licenseProduct.product, product).fetchJoin()
			.innerJoin(product.productType, productType).fetchJoin()
			.where(licenseProduct.licensePlan.eq(licensePlan)
				.and(productType.name.eq(ProductTypeId.PRODUCT.getValue())))
			.fetch();
	}

	@Override
	public List<LicenseProduct> findAllServiceLicenseInfoByLicensePlan(
		LicensePlan licensePlan
	) {
		return query.selectFrom(licenseProduct)
			.distinct()
			.innerJoin(licenseProduct.product, product).fetchJoin()
			.innerJoin(product.productType, productType).fetchJoin()
			.where(licenseProduct.licensePlan.eq(licensePlan)
				.and(productType.name.eq(ProductTypeId.SERVICE.getValue())))
			.fetch();
	}

	@Override
	public Optional<LicenseProduct> findLicenseProductByLicensePlanAndProduct(
		LicensePlan targetLicensePlan, Product targetProduct
	) {
		return Optional.ofNullable(
			query.selectFrom(licenseProduct)
				.distinct()
				.innerJoin(licenseProduct.product, product).fetchJoin()
				.innerJoin(licenseProduct.licenseList, license).fetchJoin()
				.where(licenseProduct.product.eq(targetProduct).and(licenseProduct.licensePlan.eq(targetLicensePlan)))
				.fetchFirst()
		);
	}
}
