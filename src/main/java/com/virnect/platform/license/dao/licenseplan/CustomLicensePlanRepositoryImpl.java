package com.virnect.platform.license.dao.licenseplan;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.virnect.platform.license.domain.license.QLicense;
import com.virnect.platform.license.domain.licenseplan.LicensePlan;
import com.virnect.platform.license.domain.licenseplan.PlanStatus;
import com.virnect.platform.license.domain.licenseplan.QLicensePlan;
import com.virnect.platform.license.domain.product.QLicenseProduct;
import com.virnect.platform.license.domain.product.QProduct;

public class CustomLicensePlanRepositoryImpl extends QuerydslRepositorySupport implements CustomLicensePlanRepository {
	public CustomLicensePlanRepositoryImpl() {
		super(LicensePlan.class);
	}

	@Override
	public List<LicensePlan> getMyLicenseInfoInWorkspaceLicensePlan(String workspaceId, String userId) {
		QLicensePlan qLicensePlan = QLicensePlan.licensePlan;
		QLicenseProduct qLicenseProduct = QLicenseProduct.licenseProduct;
		QLicense qLicense = QLicense.license;
		QProduct qProduct = QProduct.product;

		return from(qLicensePlan)
			.join(qLicensePlan.licenseProductList, qLicenseProduct).fetchJoin()
			.join(qLicenseProduct.product, qProduct).fetchJoin()
			.join(qLicenseProduct.licenseList, qLicense).fetchJoin()
			.where(qLicensePlan.workspaceId.eq(workspaceId)
				.and(qLicensePlan.planStatus.eq(PlanStatus.ACTIVE))
				.and(qLicense.userId.eq(userId)))
			.fetch();
	}

	@Override
	public List<LicensePlan> getExpiredLicensePlanListByCurrentDate(LocalDateTime currentDateTime) {
		QLicensePlan qLicensePlan = QLicensePlan.licensePlan;

		return from(qLicensePlan)
			.where(qLicensePlan.endDate.before(currentDateTime).and(qLicensePlan.planStatus.eq(PlanStatus.ACTIVE)))
			.fetch();
	}
}
