package com.virnect.license.dao.coupon;

import com.virnect.license.domain.coupon.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.13
 */
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponCustomRepository {
    Optional<Coupon> findByUserIdAndSerialKey(String userId, String serialKey);

    Optional<Coupon> findByUserIdAndIdAndRegisterDateIsNotNull(String userId, long couponId);

    Page<Coupon> findByUserId(String userId, Pageable pageable);

    Page<Coupon> findByUserIdAndRegisterDateIsNotNull(String userId, Pageable pageable);

    @EntityGraph(attributePaths = "couponProductList", type = EntityGraph.EntityGraphType.LOAD)
    Page<Coupon> findAll(Pageable pageable);
}
