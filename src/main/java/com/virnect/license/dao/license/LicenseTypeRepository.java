package com.virnect.license.dao.license;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.license.LicenseType;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.13
 */
public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
	Optional<LicenseType> findByName(String licenseTypeName);
}
