// package com.virnect.license;
//
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;
// import java.util.Set;
//
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
//
// import lombok.extern.slf4j.Slf4j;
//
// import com.virnect.license.dao.license.LicenseRepository;
// import com.virnect.license.dao.licenseplan.LicensePlanRepository;
// import com.virnect.license.dao.licenseproduct.LicenseProductRepository;
// import com.virnect.license.dao.product.ProductRepository;
// import com.virnect.license.domain.license.License;
// import com.virnect.license.domain.licenseplan.LicensePlan;
// import com.virnect.license.domain.licenseplan.PlanStatus;
// import com.virnect.license.domain.product.LicenseProduct;
// import com.virnect.license.domain.product.Product;
//
// @Slf4j
// @SpringBootTest
// @ActiveProfiles("local")
// public class LicenseProductArrange {
// 	@Autowired
// 	private LicensePlanRepository licensePlanRepository;
// 	@Autowired
// 	private LicenseProductRepository licenseProductRepository;
// 	@Autowired
// 	private ProductRepository productRepository;
// 	@Autowired
// 	private LicenseRepository licenseRepository;
//
// 	@Test
// 	public void reArrange() {
// 		List<LicensePlan> allLicensePlan = licensePlanRepository.findAllByPlanStatus(PlanStatus.ACTIVE);
// 		Optional<Set<Product>> productListInfo = productRepository.findByProductType_NameAndNameIsIn(
// 			"BASIC PLAN", Arrays.asList("Remote", "Make", "View"));
//
// 		if (!productListInfo.isPresent()) {
// 			log.error("product list not exist");
// 			return;
// 		}
//
// 		List<Product> productSet = (List<Product>)productListInfo.get();
//
// 		for (LicensePlan licensePlan : allLicensePlan) {
// 			// 라이선스 상품 가져오기
// 			Set<LicenseProduct> licenseProductSet = licensePlan.getLicenseProductList();
//
// 			licenseProductSet.forEach(lp -> log.info("LP: {}, Quantity: {}", lp.getId(), lp.getQuantity()));
//
// 			// 상품 별로
// 			for (Product product : productSet) {
// 				List<License> previousLicense = new ArrayList<>();
//
// 				// 해당 상품의 모든 라이선스 가져오기
// 				licenseProductSet.stream().filter(lp -> lp.getProduct().getId().equals(product.getId())).forEach(
// 					licenseProduct -> {
// 						previousLicense.addAll(licenseProduct.getLicenseList());
// 					}
// 				);
// 				// 해당 상품의 전체 갯수 계산
// 				int totalQuantity = licenseProductSet.stream()
// 					.filter(lp -> lp.getProduct().getId().equals(product.getId()))
// 					.mapToInt(
// 						LicenseProduct::getQuantity)
// 					.sum();
//
// 				log.info(
// 					"PLAN: {} , product:[{} , {}], totalQuantity: {} ", licensePlan.getId(), product.getId(),
// 					product.getName(), totalQuantity
// 				);
// 				// 새 상품 정보 생성, 전체 갯수 반영
//
// 				LicenseProduct licenseProductSum = LicenseProduct.builder()
// 					.licensePlan(licensePlan)
// 					.product(product)
// 					.quantity(totalQuantity)
// 					.build();
// 				licenseProductRepository.save(licenseProductSum);
//
// 				// // 이전 해당 상품의 모든 라이선스에 새 상품 정보 반영
// 				previousLicense.forEach(license -> {
// 					license.setLicenseProduct(licenseProductSum);
// 					licenseRepository.save(license);
// 					log.info("LICENSE: {}", license.toString());
// 				});
// 			}
//
// 			// 이전 라이선스 상품은 모두 삭제
// 			licenseProductRepository.deleteAll(licenseProductSet);
// 		}
// 	}
// }
