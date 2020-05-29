package com.virnect.license.dao;

import com.virnect.license.domain.Product;
import com.virnect.license.domain.ProductDisplayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.13
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Set<Product>> findByProductType_NameAndNameIsIn(String productType, List<String> productNameList);

    List<Product> findAllByDisplayStatus(ProductDisplayStatus status);
}
