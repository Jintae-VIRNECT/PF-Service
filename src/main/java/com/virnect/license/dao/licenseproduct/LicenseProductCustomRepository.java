package com.virnect.license.dao.licenseproduct;

import java.util.List;

import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.product.LicenseProduct;

public interface LicenseProductCustomRepository {
	List<LicenseProduct> findAllProductLicenseInfoByLicensePlan(LicensePlan licensePlan);

	List<LicenseProduct> findAllServiceLicenseInfoByLicensePlan(LicensePlan licensePlan);
}
