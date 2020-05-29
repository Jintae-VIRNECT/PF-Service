package com.virnect.license.dao;

import com.virnect.license.domain.LicensePlan;
import com.virnect.license.domain.PlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
public interface LicensePlanRepository extends JpaRepository<LicensePlan, Long>, CustomLicensePlanRepository {
    @Transactional(readOnly = true)
    Optional<LicensePlan> findByUserIdAndWorkspaceIdAndPlanStatus(String userId, String workspaceId, PlanStatus status);

    @Transactional(readOnly = true)
    Optional<LicensePlan> findByUserIdAndWorkspaceId(String userId, String workspaceId);

    Optional<LicensePlan> findByWorkspaceIdAndPlanStatus(String workspaceId, PlanStatus planStatus);

    Optional<LicensePlan> findByUserIdAndPaymentId(String userId, long paymentId);

    LicensePlan findByUserIdAndPlanStatus(String userUUID, PlanStatus status);

    Boolean existsByUserIdAndWorkspaceIdAndPlanStatus(String userUUID, String workspaceId, PlanStatus status);
}
