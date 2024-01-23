package com.virnect.license.dao.licenseproduct;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.domain.product.Product;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.14
 */
public interface LicenseProductRepository extends JpaRepository<LicenseProduct, Long>, LicenseProductCustomRepository {
	Optional<LicenseProduct> findByLicensePlanAndProduct(LicensePlan licensePlan, Product product);

	@EntityGraph(attributePaths = {"product", "licenseList"}, type = EntityGraph.EntityGraphType.LOAD)
	Optional<LicenseProduct> findByLicensePlanAndProduct_Name(LicensePlan licensePlan, String productName);
}
