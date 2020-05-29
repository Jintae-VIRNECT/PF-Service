package com.virnect.license.dao;

import com.virnect.license.domain.License;
import com.virnect.license.domain.LicenseStatus;
import com.virnect.license.domain.PlanStatus;
import com.virnect.license.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.16
 */
public interface LicenseRepository extends JpaRepository<License, Long> {
    List<License> findAllByLicenseProduct_LicensePlan_WorkspaceIdAndLicenseProduct_LicensePlan_PlanStatusAndLicenseProduct_ProductAndStatus(String workspaceId, PlanStatus planStatus, Product product, LicenseStatus status);
    License findByUserIdAndLicenseProduct_LicensePlan_WorkspaceIdAndLicenseProduct_ProductAndStatus(String userId, String workspaceId, Product product, LicenseStatus status);
}
