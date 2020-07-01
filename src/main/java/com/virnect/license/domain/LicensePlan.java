package com.virnect.license.domain;

import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.*;
import java.time.LocalDateTime;
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
@Table(name = "license_plan", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"workspace_id", "status", "created_at"}),
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LicensePlan extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "license_plan_id")
    private Long id;

    @Column(name = "start_at")
    private LocalDateTime startDate;

    @Column(name = "expired_at")
    private LocalDateTime endDate;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "max_user_amount")
    private Long maxUserAmount;

    @Column(name = "max_storage_size", nullable = false)
    private Long maxStorageSize;

    @Column(name = "max_download_hit", nullable = false)
    private Long maxDownloadHit;

    @Column(name = "max_call_time", nullable = false)
    private Long maxCallTime;

    // nullable false 로 나중에 바꾸기
    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "modified_user", nullable = false)
    @LastModifiedBy
    private String modifiedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PlanStatus planStatus = PlanStatus.INACTIVE;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "payment_id")
    private String paymentId;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "licensePlan")
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @OneToMany(mappedBy = "licensePlan", fetch = FetchType.LAZY)
    private Set<LicenseProduct> licenseProductList;

    @Builder
    public LicensePlan(String userId, String workspaceId, LocalDateTime startDate, LocalDateTime endDate, PlanStatus planStatus, Coupon coupon, Long maxDownloadHit, Long maxStorageSize, Long maxCallTime, String paymentId, Long maxUserAmount, String countryCode) {
        this.userId = userId;
        this.workspaceId = workspaceId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.planStatus = planStatus;
        this.coupon = coupon;
        this.maxDownloadHit = maxDownloadHit;
        this.maxCallTime = maxCallTime;
        this.maxStorageSize = maxStorageSize;
        this.modifiedUser = userId;
        this.paymentId = paymentId;
        this.maxUserAmount = maxUserAmount;
        this.countryCode = countryCode;
    }
}
