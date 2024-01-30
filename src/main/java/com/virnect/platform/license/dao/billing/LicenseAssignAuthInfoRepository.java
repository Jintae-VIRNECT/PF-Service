package com.virnect.platform.license.dao.billing;

import org.springframework.data.repository.CrudRepository;

import com.virnect.platform.license.domain.billing.LicenseAssignAuthInfo;

public interface LicenseAssignAuthInfoRepository extends CrudRepository<LicenseAssignAuthInfo, String> {
}
