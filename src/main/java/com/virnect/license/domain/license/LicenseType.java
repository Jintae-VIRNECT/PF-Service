package com.virnect.license.domain.license;

import com.virnect.license.domain.BaseTimeEntity;
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
@Table(name = "license_Type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicenseType extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_type_id")
    private Long id;

    @Column(name = "name")
    private String name;

    public LicenseType(String licenseType) {
        this.name = licenseType;
    }
}
