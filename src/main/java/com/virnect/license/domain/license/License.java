package com.virnect.license.domain.license;

import com.virnect.license.domain.BaseTimeEntity;
import com.virnect.license.domain.product.LicenseProduct;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;

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
@Table(name = "license")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class License extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_id")
    private Long id;

    @Column(name = "serial_key")
    private String serialKey;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "license_status")
    private LicenseStatus status = LicenseStatus.UNUSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_product_id")
    private LicenseProduct licenseProduct;

    @Builder
    public License(String serialKey, LicenseStatus status, LicenseProduct licenseProduct) {
        this.serialKey = serialKey;
        this.status = status;
        this.licenseProduct = licenseProduct;
    }
}
