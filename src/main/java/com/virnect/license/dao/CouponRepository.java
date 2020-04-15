package com.virnect.license.dao;

import com.virnect.license.domain.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Coupon> findByUserId(String userId, Pageable pageable);
}
