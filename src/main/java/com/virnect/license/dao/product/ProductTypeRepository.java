package com.virnect.license.dao.product;

import com.virnect.license.domain.product.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.13
 */
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
}
