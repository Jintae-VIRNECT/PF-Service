package com.virnect.license.dao.licenseplan;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.licenseplan.PlanStatus;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
public interface LicensePlanRepository extends JpaRepository<LicensePlan, Long>, CustomLicensePlanRepository {
	@Transactional(readOnly = true)
	LicensePlan findByUserIdAndWorkspaceIdAndPlanStatus(String userId, String workspaceId, PlanStatus status);

	@Transactional(readOnly = true)
	Optional<LicensePlan> findByUserIdAndWorkspaceId(String userId, String workspaceId);

	Optional<LicensePlan> findByWorkspaceIdAndPlanStatus(String workspaceId, PlanStatus planStatus);

	Optional<LicensePlan> findByUserIdAndPaymentId(String userId, String paymentId);

	Optional<LicensePlan> findByUserIdAndPlanStatus(String userUUID, PlanStatus status);

	boolean existsByUserId(String userUUID);
	List<LicensePlan> findAllByPlanStatus(PlanStatus planStatus);
}
