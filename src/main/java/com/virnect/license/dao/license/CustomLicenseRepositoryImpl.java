package com.virnect.license.dao.license;

import static com.virnect.license.domain.license.QLicense.*;
import static com.virnect.license.domain.licenseplan.PlanStatus.*;
import static com.virnect.license.domain.licenseplan.QLicensePlan.*;
import static com.virnect.license.domain.product.QLicenseProduct.*;
import static com.virnect.license.domain.product.QProduct.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.dto.UserLicenseDetailsInfo;

@Slf4j
@RequiredArgsConstructor
public class CustomLicenseRepositoryImpl implements CustomLicenseRepository {
	private final JPAQueryFactory query;

	@Override
	public Page<UserLicenseDetailsInfo> findAllMyLicenseInfo(String userId, Pageable pageable) {
		JPQLQuery<UserLicenseDetailsInfo> findQuery = query.selectFrom(license)
			.select(Projections.constructor(
				UserLicenseDetailsInfo.class,
				licensePlan.workspaceId,
				product.name,
				licensePlan.endDate,
				licenseProduct.status.as("productPlanStatus")
			))
			.join(licenseProduct).on(license.licenseProduct.eq(licenseProduct)).fetchJoin()
			.join(product).on(licenseProduct.product.eq(product)).fetchJoin()
			.join(licensePlan).on(licenseProduct.licensePlan.eq(licensePlan)).fetchJoin()
			.where(licensePlan.planStatus.ne(TERMINATE))
			.where(license.userId.eq(userId).and(product.name.in(Arrays.asList("MAKE", "VIEW", "REMOTE"))));

		findQuery.offset(pageable.getOffset());
		findQuery.limit(pageable.getPageSize());
		List<UserLicenseDetailsInfo> userLicenseDetailsInfoList = findQuery.fetch();
		return new PageImpl<>(userLicenseDetailsInfoList, pageable, findQuery.fetchCount());
	}

	@Override
	public long updateAllLicenseInfoInactiveByLicenseProduct(
		Set<LicenseProduct> licenseProductSet
	) {
		return query.update(license)
			.set(license.status, LicenseStatus.UNUSE)
			.setNull(license.userId)
			.where(license.licenseProduct.in(licenseProductSet))
			.execute();
	}

	@Override
	public long deleteAllLicenseByLicenseIdIn(List<Long> deleteLicenseIdList) {
		return query.delete(license)
			.where(license.id.in(deleteLicenseIdList))
			.execute();
	}
}
