package com.virnect.license.dao.licenseplan;

import com.virnect.license.domain.*;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CustomLicensePlanRepositoryImpl extends QuerydslRepositorySupport implements CustomLicensePlanRepository {
    public CustomLicensePlanRepositoryImpl() {
        super(LicensePlan.class);
    }

    @Override
    public List<LicensePlan> getMyLicenseInfoInWorkspaceLicensePlan(String workspaceId, String userId) {
        QLicensePlan qLicensePlan = QLicensePlan.licensePlan;
        QLicenseProduct qLicenseProduct = QLicenseProduct.licenseProduct;
        QLicense qLicense = QLicense.license;
        QLicenseType qLicenseType = QLicenseType.licenseType;
        QProduct qProduct = QProduct.product;

        return from(qLicensePlan)
                .join(qLicensePlan.licenseProductList, qLicenseProduct)
                .fetchJoin()
                .join(qLicenseProduct.product, qProduct)
                .fetchJoin()
                .join(qLicenseProduct.licenseType, qLicenseType)
                .fetchJoin()
                .join(qLicenseProduct.licenseList, qLicense)
                .fetchJoin()
                .where(qLicensePlan.workspaceId.eq(workspaceId).and(qLicensePlan.planStatus.eq(PlanStatus.ACTIVE)).and(qLicense.userId.eq(userId)))
                .fetch();
    }
}
