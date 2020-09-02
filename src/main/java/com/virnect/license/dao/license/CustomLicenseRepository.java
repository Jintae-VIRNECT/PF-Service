package com.virnect.license.dao.license;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.virnect.license.domain.product.LicenseProduct;
import com.virnect.license.dto.UserLicenseDetailsInfo;

public interface CustomLicenseRepository {
	Page<UserLicenseDetailsInfo> findAllMyLicenseInfo(String userId, Pageable pageable);

	long updateAllLicenseInfoInactiveByLicenseProduct(Set<LicenseProduct> licenseProductSet);
}
