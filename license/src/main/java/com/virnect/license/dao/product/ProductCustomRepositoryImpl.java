package com.virnect.license.dao.product;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.virnect.license.domain.product.Product;
import com.virnect.license.domain.product.ProductStatus;
import com.virnect.license.domain.product.QProduct;

public class ProductCustomRepositoryImpl extends QuerydslRepositorySupport implements ProductCustomRepository {
	public ProductCustomRepositoryImpl() {
		super(Product.class);
	}

	@Override
	public long updateProductDisplayStatusToInactive(long productId) {
		QProduct qProduct = QProduct.product;
		return update(qProduct).set(qProduct.displayStatus, ProductStatus.INACTIVE)
			.where(qProduct.id.eq(productId))
			.execute();
	}
}
