package com.virnect.license.dao.license;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.PlanStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.16
 */
public interface LicenseRepository extends JpaRepository<License, Long>, CustomLicenseRepository {
	List<License> findAllByLicenseProduct_LicensePlan_WorkspaceIdAndLicenseProduct_LicensePlan_PlanStatusAndLicenseProduct_ProductAndStatus(
		String workspaceId, PlanStatus planStatus, Product product, LicenseStatus status
	);

	License findByUserIdAndLicenseProduct_LicensePlan_WorkspaceIdAndLicenseProduct_ProductAndStatus(
		String userId, String workspaceId, Product product, LicenseStatus status
	);

	long countByLicenseProductAndStatus(LicenseProduct licenseProduct, LicenseStatus status);
}
