package com.virnect.license.dao.product;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.product.LicenseProduct;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.14
 */
public interface LicenseProductRepository extends JpaRepository<LicenseProduct, Long> {
}
