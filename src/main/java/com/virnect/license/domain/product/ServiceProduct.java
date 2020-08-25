package com.virnect.license.domain.product;

import com.virnect.license.domain.BaseTimeEntity;
import com.virnect.license.domain.licenseplan.LicensePlan;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Audited
@Table(name = "service_product",
indexes = {
        @Index(name = "IDX_service_product_type_with_plan", columnList = "license_plan_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceProduct extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_product_id")
    private Long id;

    @Column(name = "total_storage_size", nullable = false)
    private Long totalStorageSize;

    @Column(name = "total_download_hit", nullable = false)
    private Long totalDownloadHit;

    @Column(name = "total_call_time", nullable = false)
    private Long totalCallTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_plan_id", nullable = false)
    private LicensePlan licensePlan;

    @Builder
    public ServiceProduct(Long totalStorageSize, Long totalDownloadHit, Long totalCallTime, LicensePlan licensePlan) {
        this.totalStorageSize = totalStorageSize;
        this.totalDownloadHit = totalDownloadHit;
        this.totalCallTime = totalCallTime;
        this.licensePlan = licensePlan;
    }
}
