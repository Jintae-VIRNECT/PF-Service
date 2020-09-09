package com.virnect.license.dao.billing;

import org.springframework.data.repository.CrudRepository;

import com.virnect.license.domain.billing.LicenseAssignAuthInfo;

public interface LicenseAssignAuthInfoRepository extends CrudRepository<LicenseAssignAuthInfo, String> {
}
