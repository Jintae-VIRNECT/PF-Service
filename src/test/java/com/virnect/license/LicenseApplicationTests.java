package com.virnect.license;

import com.virnect.license.dao.coupon.CouponRepository;
import com.virnect.license.dao.license.LicenseTypeRepository;
import com.virnect.license.dao.product.ProductRepository;
import com.virnect.license.dao.product.ProductTypeRepository;
import com.virnect.license.domain.coupon.Coupon;
import com.virnect.license.domain.product.Product;
import com.virnect.license.domain.product.ProductType;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles(value = "local")
class LicenseApplicationTests {
    @Autowired
    LicenseTypeRepository licenseTypeRepository;
    @Autowired
    ProductTypeRepository productTypeRepository;
    @Autowired
    ProductRepository productRepository;

    @Autowired
    CouponRepository couponRepository;

    LicenseType[] licenseTypes = new LicenseType[]{
            new LicenseType("2_WEEK_FREE_TRIAL_LICENSE"),
            new LicenseType("DEMO"),
            new LicenseType("DEV"),
            new LicenseType("CONTACT"),
            new LicenseType("SASS"),
            new LicenseType("TRIAL_LICENSE")};

    String[] productNames = {"REMOTE", "MAKE", "VIEW"};
    ProductType[] productTypes = new ProductType[]{
            new ProductType("BASIC"),
            new ProductType("TEAM"),
            new ProductType("PRO"),
    };

    @Test
    public void 라이선스_타입_데이터_넣기() {
        for (LicenseType licenseType : licenseTypes) {
            this.licenseTypeRepository.save(licenseType);
        }
    }

    @Test
    public void 상품_타입_정보_넣기() {
        for (ProductType productType : productTypes) {
            this.productTypeRepository.save(productType);
        }
    }

    @Test
    public void 상품_정보_넣기() {
        List<ProductType> productTypes = this.productTypeRepository.findAll();

        for (ProductType productType : productTypes) {
            for (String productName : productNames) {
                this.productRepository.save(new Product(productName, 1000, productType));
            }
        }
    }

    @Test
    public void 쿠폰_시리얼_정보_입력() {
        List<Coupon> copCoupons = couponRepository.findAll();
        for (Coupon coupon : copCoupons) {
            if (StringUtils.isEmpty(coupon.getSerialKey())) {
                coupon.setSerialKey(UUID.randomUUID().toString().toUpperCase());
                couponRepository.save(coupon);
                System.out.println(coupon);
            }
        }
    }
}
