package com.virnect.license.dao;

import com.virnect.license.domain.LicenseAssignAuthInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LicenseAssignAuthInfoRepository extends CrudRepository<LicenseAssignAuthInfo, String> {
}
