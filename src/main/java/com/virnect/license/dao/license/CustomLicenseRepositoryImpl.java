package com.virnect.license.dao.license;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.QLicense;
import com.virnect.license.domain.licenseplan.QLicensePlan;
import com.virnect.license.domain.product.QLicenseProduct;
import com.virnect.license.domain.product.QProduct;
import com.virnect.license.dto.UserLicenseDetailsInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;    
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CustomLicenseRepositoryImpl extends QuerydslRepositorySupport implements CustomLicenseRepository {
    public CustomLicenseRepositoryImpl() {
        super(License.class);
    }

    @Override
    public Page<UserLicenseDetailsInfo> findAllMyLicenseInfo(String userId, Pageable pageable) {
        QLicensePlan qLicensePlan = QLicensePlan.licensePlan;
        QLicense qLicense = QLicense.license;
        QLicenseProduct qLicenseProduct = QLicenseProduct.licenseProduct;
        QProduct qProduct = QProduct.product;

        JPQLQuery<UserLicenseDetailsInfo> query = from(qLicense)
                .select(Projections.constructor(UserLicenseDetailsInfo.class,
                        qLicensePlan.workspaceId,
                        qProduct.name,
                        qLicensePlan.endDate
                ))
                .join(qLicenseProduct).on(qLicense.licenseProduct.eq(qLicenseProduct)).fetchJoin()
                .join(qProduct).on(qLicenseProduct.product.eq(qProduct)).fetchJoin()
                .join(qLicensePlan).on(qLicenseProduct.licensePlan.eq(qLicensePlan)).fetchJoin()
                .where(qLicense.userId.eq(userId));

        List<UserLicenseDetailsInfo> userLicenseDetailsInfoList = getQuerydsl().applyPagination(pageable, query).fetch();

        return new PageImpl<>(userLicenseDetailsInfoList, pageable, query.fetchCount());
    }
}
