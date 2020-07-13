package com.virnect.license.dao.license;

import com.virnect.license.dto.UserLicenseDetailsInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomLicenseRepository {
    Page<UserLicenseDetailsInfo> findAllMyLicenseInfo(String userId, Pageable pageable);
}
