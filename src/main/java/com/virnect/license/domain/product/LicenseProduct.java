package com.virnect.license.domain.product;

import com.virnect.license.domain.BaseTimeEntity;
import com.virnect.license.domain.license.License;
import com.virnect.license.domain.licenseplan.LicensePlan;
import com.virnect.license.domain.license.LicenseType;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
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
    public LicenseProduct(Integer quantity, LicensePlan licensePlan, Product product, LicenseType licenseType) {
        this.quantity = quantity;
        this.licensePlan = licensePlan;
        this.product = product;
        this.licenseType = licenseType;
    }
}
