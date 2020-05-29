package com.virnect.license.domain;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Entity
@Getter
@Setter
@Audited
@Table(name = "license_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicenseProduct extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_product_id")
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_plan_id")
    private LicensePlan licensePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_type_id")
    private LicenseType licenseType;

    @OneToMany(mappedBy = "licenseProduct", fetch = FetchType.LAZY)
    private Set<License> licenseList;

    @Builder
    public LicenseProduct(Integer quantity, Long price, LicensePlan licensePlan, Product product, LicenseType licenseType, Coupon coupon) {
        this.quantity = quantity;
        this.price = price;
        this.licensePlan = licensePlan;
        this.product = product;
        this.coupon = coupon;
        this.licenseType = licenseType;
    }
}
