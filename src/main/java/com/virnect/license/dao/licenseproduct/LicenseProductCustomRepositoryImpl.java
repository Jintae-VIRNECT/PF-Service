package com.virnect.license.dao.licenseproduct;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.virnect.license.domain.billing.ProductTypeId;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.QLicenseProduct;
import com.virnect.license.domain.product.QProduct;
import com.virnect.license.domain.product.QProductType;

public class LicenseProductCustomRepositoryImpl extends QuerydslRepositorySupport
	implements LicenseProductCustomRepository {

	public LicenseProductCustomRepositoryImpl() {
		super(LicenseProduct.class);
	}

	@Override
	public List<LicenseProduct> findAllProductLicenseInfoByLicensePlan(
		LicensePlan licensePlan
	) {
		QLicenseProduct qLicenseProduct = QLicenseProduct.licenseProduct;
		QProduct qProduct = QProduct.product;
		QProductType qProductType = QProductType.productType;
		return from(qLicenseProduct)
			.innerJoin(qLicenseProduct.product, qProduct).fetchJoin()
			.innerJoin(qProduct.productType, qProductType).fetchJoin()
			.where(qLicenseProduct.licensePlan.eq(licensePlan)
				.and(qProductType.name.eq(ProductTypeId.PRODUCT.getPlan())))
			.fetch();
	}

	@Override
	public List<LicenseProduct> findAllServiceLicenseInfoByLicensePlan(
		LicensePlan licensePlan
	) {
		QLicenseProduct qLicenseProduct = QLicenseProduct.licenseProduct;
		QProduct qProduct = QProduct.product;
		QProductType qProductType = QProductType.productType;
		return from(qLicenseProduct)
			.innerJoin(qLicenseProduct.product, qProduct).fetchJoin()
			.innerJoin(qProduct.productType, qProductType).fetchJoin()
			.where(qLicenseProduct.licensePlan.eq(licensePlan)
				.and(qProductType.name.eq(ProductTypeId.SERVICE.getPlan())))
			.fetch();
	}
}
