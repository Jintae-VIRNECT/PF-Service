package com.virnect.license.dao.product;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.product.ServiceProduct;

public interface ServiceProductRepository extends JpaRepository<ServiceProduct, Long> {
}
