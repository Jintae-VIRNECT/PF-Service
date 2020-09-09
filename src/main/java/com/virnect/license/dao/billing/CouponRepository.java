package com.virnect.license.dao.billing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.virnect.license.domain.billing.Coupon;
import com.virnect.license.domain.licenseplan.LicensePlan;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
	List<Coupon> findByLicensePlan(LicensePlan licensePlan);
}
