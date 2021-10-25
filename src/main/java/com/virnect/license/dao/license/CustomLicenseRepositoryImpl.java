package com.virnect.license.dao.license;

import static com.virnect.license.domain.license.QLicense.*;
import static com.virnect.license.domain.licenseplan.PlanStatus.*;
import static com.virnect.license.domain.licenseplan.QLicensePlan.*;
import static com.virnect.license.domain.product.QLicenseProduct.*;
import static com.virnect.license.domain.product.QProduct.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.domain.license.License;
import com.virnect.license.domain.license.LicenseStatus;
import com.virnect.license.domain.licenseplan.LicensePlan;
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

	@Override
	public License licenseAllocationRevokeByUserIdAndLicenseProductId(String userId, long licenseProductId) {
		return query.selectFrom(license)
			.where(license.licenseProduct.id.eq(licenseProductId).and(license.userId.eq(userId)))
			.fetchOne();
	}

	@Override
	public Optional<License> findAllocatableLicensesByLicenseProduct(
		LicenseProduct licenseProduct
	) {
		return Optional.ofNullable(
			query.selectFrom(license)
				.where(license.licenseProduct.eq(licenseProduct))
				.where(license.status.eq(LicenseStatus.UNUSE))
				.fetchFirst()
		);
	}

	@Override
	public long revertAllLicenseByUserUUID(String userUUID) {
		return query.update(license)
			.where(license.userId.eq(userUUID))
			.set(license.status, LicenseStatus.UNUSE)
			.execute();
	}

	@Override
	public List<License> findAllLicenseByUserUUIDListAndLicensePlanAndProductName(
		LicensePlan targetLicensePlan, String productName, List<String> userUUIDList
	) {
		return query.selectFrom(license)
			.innerJoin(license.licenseProduct, licenseProduct).fetchJoin()
			.innerJoin(licenseProduct.licensePlan, licensePlan).fetchJoin()
			.innerJoin(licenseProduct.product, product).fetchJoin()
			.where(license.userId.in(userUUIDList), matchProductName(productName)).fetch();
	}

	private BooleanExpression matchProductName(String productName) {
		if (StringUtils.isEmpty(productName)) {
			return null;
		}
		return product.name.eq(productName);
	}
}
