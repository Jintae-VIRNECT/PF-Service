package com.virnect.license.dao.product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.product.Product;
import com.virnect.license.domain.product.ProductStatus;

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
