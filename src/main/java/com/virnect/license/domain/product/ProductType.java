package com.virnect.license.domain.product;

import com.virnect.license.domain.BaseTimeEntity;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
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
@Table(name = "product_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductType extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_type_id")
    Long id;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "productType", fetch = FetchType.LAZY)
    private List<Product> productList = new ArrayList<>();

    public ProductType(String productTypeName) {
        this.name = productTypeName;
    }
}
