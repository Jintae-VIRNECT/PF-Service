package com.virnect.platform.license.dao.applicense;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.platform.license.domain.applicense.AppLicense;

public interface AppLicenseRepository extends JpaRepository<AppLicense, Long> {
	boolean existsBySerialKey(String serialKey);

	Optional<AppLicense> findBySerialKey(String serialKey);

	Page<AppLicense> findAllBySerialKeyContaining(String search, Pageable pageable);
}
