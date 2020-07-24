package com.virnect.license.dao.coupon;

import com.virnect.license.domain.coupon.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.14
 */
public interface CouponCustomRepository {
    Page<Coupon> findMyCouponListByUserIdAndPageable(String userId, Pageable pageable);

    Page<Coupon> findAllCouponInfo(Pageable pageable);
}
