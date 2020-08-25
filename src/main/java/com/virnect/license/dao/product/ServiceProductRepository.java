package com.virnect.license.dao.product;

import com.virnect.license.domain.product.ServiceProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProductRepository extends JpaRepository<ServiceProduct, Long> {
}
