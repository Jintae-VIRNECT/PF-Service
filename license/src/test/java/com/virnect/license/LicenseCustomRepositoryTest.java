package com.virnect.license;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.licenseplan.LicensePlan;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("local")
public class LicenseCustomRepositoryTest {
	@Autowired
	LicensePlanRepository licensePlanRepository;

	@Autowired
	LicenseRepository licenseRepository;

	private List<String> userUUIDList = Arrays.asList("a", "b", "c");
	private String productName = "REMOTE";

	@Test
	@DisplayName("Querydsl 잘돌아가니..?")
	public void findAllUserLicenseInfoWithProductNameTest() {
		Optional<LicensePlan> licensePlanOptional = licensePlanRepository.findById(249L);
		LicensePlan licensePlan = licensePlanOptional.get();
		List<License> userLicenseInfo = licenseRepository.findAllLicenseByUserUUIDListAndLicensePlanAndProductName(
			licensePlan, productName, userUUIDList);

		for (License license : userLicenseInfo) {
			System.out.println("license = " + license);
			assertThat(license.getUserId()).isIn(userUUIDList);
			assertThat(license.getLicenseProduct().getProduct().getName()).isEqualTo(productName);
			assertThat(license.getLicenseProduct().getLicensePlan().getId()).isEqualTo(licensePlan.getId());
		}
	}

	@Test
	@DisplayName("Querydsl 잘돌아가니..?")
	public void findAllUserLicenseInfoWithProductNameTest2() {
		Optional<LicensePlan> licensePlanOptional = licensePlanRepository.findById(249L);
		LicensePlan licensePlan = licensePlanOptional.get();
		List<License> userLicenseInfo = licenseRepository.findAllLicenseByUserUUIDListAndLicensePlanAndProductName(
			licensePlan, null, userUUIDList);

		for (License license : userLicenseInfo) {
			System.out.println("license = " + license);
			assertThat(license.getUserId()).isIn(userUUIDList);
			// assertThat(license.getLicenseProduct().getProduct().getName()).isEqualTo(productName);
			assertThat(license.getLicenseProduct().getLicensePlan().getId()).isEqualTo(licensePlan.getId());
		}
	}
}
