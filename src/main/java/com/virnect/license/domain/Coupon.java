package com.virnect.license.domain;

import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long id;

    @Column(name = "company")
    private String company;

    @Column(name = "department")
    private String department;

    @Column(name = "position")
    private String position;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "call_number")
    private String callNumber;

    @Column(name = "company_site")
    private String companySite;

    @Column(name = "company_service")
    private String companyService;

    @Column(name = "comany_worker")
    private String companyWorker;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "market_info_receive", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status marketInfoReceive = Status.REJECT;

    @Column(name = "expired_at")
    private LocalDateTime expiredDate;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "period_type")
    @Enumerated(EnumType.STRING)
    private CouponPeriodType couponPeriodType;

    @Column(name = "serial_key", nullable = false)
    private String serialKey;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "register_at")
    private LocalDateTime registerDate;

    @Column(name = "start_at")
    private LocalDateTime startDate;

    @Column(name = "end_at")
    private LocalDateTime endDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CouponStatus status = CouponStatus.UNUSE;

    @OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY)
    List<LicenseProduct> couponProductList = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_plan_id")
    LicensePlan licensePlan;

    @Builder
    public Coupon(String company, String department, String position, String companyEmail,
                  String callNumber, String companySite, String companyService,
                  String companyWorker, String content, Status marketInfoReceive, LocalDateTime expiredDate, Long duration,
                  CouponPeriodType periodType, CouponStatus couponStatus, String userId, String serialKey, String name) {
        this.company = company;
        this.department = department;
        this.position = position;
        this.companyEmail = companyEmail;
        this.callNumber = callNumber;
        this.companySite = companySite;
        this.companyService = companyService;
        this.companyWorker = companyWorker;
        this.content = content;
        this.marketInfoReceive = marketInfoReceive;
        this.duration = duration;
        this.couponPeriodType = periodType;
        this.status = couponStatus;
        this.userId = userId;
        this.expiredDate = expiredDate;
        this.serialKey = serialKey;
        this.name = name;
    }

    /**
     * 쿠폰 만료 여부
     *
     * @return
     */
    public boolean isExpired() {
        return this.expiredDate.isBefore(LocalDateTime.now()) || this.status.equals(CouponStatus.EXPIRED);
    }

    /**
     * 쿠폰 사용 여부
     *
     * @return
     */
    public boolean isUsed() {
        return this.status.equals(CouponStatus.USE);
    }
}
