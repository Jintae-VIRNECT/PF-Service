package com.virnect.platform.license.schedule;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.license.dao.licenseplan.LicensePlanRepository;
import com.virnect.platform.license.dao.licenseproduct.LicenseProductRepository;
import com.virnect.platform.license.domain.license.LicenseStatus;
import com.virnect.platform.license.domain.licenseplan.LicensePlan;
import com.virnect.platform.license.domain.licenseplan.PlanStatus;
import com.virnect.platform.license.domain.product.LicenseProductStatus;
import com.virnect.platform.license.domain.product.Product;
import com.virnect.platform.license.event.license.LicenseExpiredEvent;

@Slf4j
@Profile(value = {"onpremise", "local"})
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class LicenseTerminateSchedule {

	private static final String SCHEDULE_NAME = "LICENSE_TERMINATE_CHECK_SCHEDULE_BY_OP_LICENSE_SERVER";
	private final LicensePlanRepository licensePlanRepository;
	private final LicenseProductRepository licenseProductRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	@PostConstruct
	public void licenseTerminateCheckScheduleBeanPostConstructor() {
		log.info(" ----> [{}] is initialized and ready.", SCHEDULE_NAME);
	}

	@Scheduled(cron = "${onpremise.batch.cron.licenseTerminate:0 0 1 * * *}", zone = "${onpremise.batch.timezone:Asia/Seoul}")
	@Transactional
	public void licenseTerminateCheckSchedule() {
		// get current date and add 9 hour for kst
		LocalDateTime localDateTime = LocalDateTime.now().plusHours(9);
		log.info("[LICENSE_SERVER_SCHEDULE][LICENSE_TERMINATE_CHECK] - CHECK DATETIME : [{}]", localDateTime);
		List<LicensePlan> expiredLicensePlanList = licensePlanRepository.getExpiredLicensePlanListByCurrentDate(
			localDateTime);

		// check exist
		if (expiredLicensePlanList.size() > 0) {
			for (LicensePlan licensePlan : expiredLicensePlanList) {
				log.info("[LICENSE_SERVER_SCHEDULE][LICENSE_PLAN][BEFORE] - {}", licensePlan.toString());
				licensePlan.getLicenseProductList().forEach(licenseProduct -> {
					log.info("[LICENSE_SERVER_SCHEDULE][LICENSE_PRODUCT][BEFORE] - {}", licensePlan.toString());
					licenseProduct.setStatus(LicenseProductStatus.INACTIVE);
					Product product = licenseProduct.getProduct();
					Map<Object, Object> pushContent = new HashMap<>();
					pushContent.put("productName", product.getName());
					licenseProduct.getLicenseList().forEach(license -> {
						log.info("[LICENSE_SERVER_SCHEDULE][LICENSE][BEFORE] - {}", license.toString());
						license.setStatus(LicenseStatus.TERMINATE);
						log.info("[LICENSE_SERVER_SCHEDULE][LICENSE][AFTER] - {}", license.toString());
						applicationEventPublisher.publishEvent(new LicenseExpiredEvent(
							product.getName().toLowerCase(), licensePlan.getWorkspaceId(),
							license.getUserId(), pushContent
						));
					});
					licenseProductRepository.save(licenseProduct);
					log.info("[LICENSE_SERVER_SCHEDULE][LICENSE_PRODUCT][AFTER] - {}", licenseProduct.toString());
				});
				licensePlan.setPlanStatus(PlanStatus.TERMINATE);
				licensePlan.setModifiedUser(SCHEDULE_NAME);
				licensePlanRepository.save(licensePlan);
				log.info("[LICENSE_SERVER_SCHEDULE][LICENSE_PLAN][AFTER] - {}", licensePlan.toString());
			}
		}
	}
}
