package com.virnect.platform.license.dao.licenseproduct;

import java.util.List;
import java.util.Optional;

import com.virnect.platform.license.domain.licenseplan.LicensePlan;
import com.virnect.platform.license.domain.product.LicenseProduct;
import com.virnect.platform.license.domain.product.Product;

public interface LicenseProductCustomRepository {
	List<LicenseProduct> findAllProductLicenseInfoByLicensePlan(LicensePlan licensePlan);

	List<LicenseProduct> findAllServiceLicenseInfoByLicensePlan(LicensePlan licensePlan);

	Optional<LicenseProduct> findLicenseProductByLicensePlanAndProduct(LicensePlan licensePlan, Product product);
}
