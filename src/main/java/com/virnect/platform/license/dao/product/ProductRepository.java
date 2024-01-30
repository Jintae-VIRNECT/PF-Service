package com.virnect.platform.license.dao.product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.platform.license.domain.product.Product;
import com.virnect.platform.license.domain.product.ProductStatus;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.13
 */
public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository {
	Optional<Set<Product>> findByProductType_NameAndNameIsIn(String productType, List<String> productNameList);

	Optional<Product> findByBillProductId(long billProductId);

	List<Product> findAllByDisplayStatus(ProductStatus status);
}
