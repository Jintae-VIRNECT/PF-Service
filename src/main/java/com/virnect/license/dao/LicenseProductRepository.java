package com.virnect.license.dao;

import com.virnect.license.domain.LicenseProduct;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.14
 */
public interface LicenseProductRepository extends JpaRepository<LicenseProduct, Long> {
    boolean existsByLicenseType_NameAndAndCoupon_UserId(String LicenseTypeName, String userUUID);
}
