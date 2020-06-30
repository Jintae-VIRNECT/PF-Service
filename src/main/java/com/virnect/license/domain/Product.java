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
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "porduct_id")
    Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "max_storage_size", nullable = false)
    private Long maxStorageSize;

    @Column(name = "max_download_hit", nullable = false)
    private Long maxDownloadHit;

    @Column(name = "max_call_time", nullable = false)
    private Long maxCallTime;

    @Column(name = "product_category")
    private String category;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @Column(name = "display", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductDisplayStatus displayStatus;

    @Builder
    public Product(String name, Long maxStorageSize, Long maxDownloadHit, Long maxCallTime, ProductType productType, String category) {
        this.name = name;
        this.maxStorageSize = maxStorageSize;
        this.maxDownloadHit = maxDownloadHit;
        this.maxCallTime = maxCallTime;
        this.productType = productType;
        this.displayStatus = ProductDisplayStatus.DISPLAY; // new product info is will not displayed by default
        this.category = category;
    }
}
