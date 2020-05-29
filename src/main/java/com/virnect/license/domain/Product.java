package com.virnect.license.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(name = "price")
    Long price;

    @Column(name = "max_storage_size", nullable = false)
    private Long maxStorageSize;

    @Column(name = "max_download_hit", nullable = false)
    private Long maxDownloadHit;

    @Column(name = "max_call_time", nullable = false)
    private Long maxCallTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    public Product(String name, Long price, ProductType productType) {
        this.name = name;
        this.price = price;
        this.productType = productType;
    }
}
