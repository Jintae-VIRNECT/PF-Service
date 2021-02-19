package com.virnect.license.dao.licenseproduct;

import java.util.List;
import java.util.Optional;

import com.virnect.license.domain.license.License;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;

public interface LicenseProductCustomRepository {
	List<LicenseProduct> findAllProductLicenseInfoByLicensePlan(LicensePlan licensePlan);

	List<LicenseProduct> findAllServiceLicenseInfoByLicensePlan(LicensePlan licensePlan);

	Optional<LicenseProduct> findLicenseProductByLicensePlanAndProduct(LicensePlan licensePlan, Product product);
}
