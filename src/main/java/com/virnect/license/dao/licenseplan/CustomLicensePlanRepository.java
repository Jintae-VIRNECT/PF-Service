package com.virnect.license.dao.licenseplan;

import com.virnect.license.domain.licenseplan.LicensePlan;

import java.util.List;

public interface CustomLicensePlanRepository {
    List<LicensePlan> getMyLicenseInfoInWorkspaceLicensePlan(String workspaceId, String userId);
}
