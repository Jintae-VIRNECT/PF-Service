package com.virnect.license.dao.licenseplan;

import java.util.List;

import com.virnect.license.domain.licenseplan.LicensePlan;

public interface CustomLicensePlanRepository {
	List<LicensePlan> getMyLicenseInfoInWorkspaceLicensePlan(String workspaceId, String userId);
}
