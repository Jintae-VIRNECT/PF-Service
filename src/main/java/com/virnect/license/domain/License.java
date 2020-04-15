package com.virnect.license.domain;

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

    @Column(name = "license_status")
    private LicenseStatus status = LicenseStatus.UNUSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ilcense_plan_id")
    private LicensePlan licensePlan;

    @Builder
    public License(String serialKey, LicenseStatus status, LicensePlan licensePlan) {
        this.serialKey = serialKey;
        this.status = status;
        this.licensePlan = licensePlan;
    }
}
