package com.virnect.license.dao;

import com.virnect.license.domain.Product;
import com.virnect.license.domain.ProductDisplayStatus;
import com.virnect.license.domain.QProduct;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class ProductCustomRepositoryImpl extends QuerydslRepositorySupport implements ProductCustomRepository {
    public ProductCustomRepositoryImpl() {
        super(Product.class);
    }

    @Override
    public long updateProductDisplayStatusToHide(long productId) {
        QProduct qProduct = QProduct.product;
        return update(qProduct).set(qProduct.displayStatus, ProductDisplayStatus.HIDE).where(qProduct.id.eq(productId)).execute();
    }
}
