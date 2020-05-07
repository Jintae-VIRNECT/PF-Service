package com.virnect.license.dao;

import com.querydsl.jpa.JPQLQuery;
import com.virnect.license.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.14
 */
public class CouponCustomRepositoryImpl extends QuerydslRepositorySupport implements CouponCustomRepository {
    public CouponCustomRepositoryImpl() {
        super(Coupon.class);
    }

    @Override
    public Page<Coupon> findMyCouponListByUserIdAndPageable(String userId, Pageable pageable) {
        QCoupon qCoupon = QCoupon.coupon;
        QLicensePlan qLicensePlan = QLicensePlan.licensePlan;
        JPQLQuery<Coupon> query = from(qCoupon).innerJoin(qCoupon.licensePlan, qLicensePlan).where(qCoupon.userId.eq(userId));
        final List<Coupon> couponList = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<>(couponList, pageable, query.fetchCount());
    }

    @Override
    public Page<Coupon> findAllCouponInfo(Pageable pageable) {
        QCoupon qCoupon = QCoupon.coupon;
        QCouponProduct qCouponProduct = QCouponProduct.couponProduct;
        QProduct qProduct = QProduct.product;
        JPQLQuery<Coupon> query = from(qCoupon)
                .innerJoin(qCoupon.couponProductList, qCouponProduct)
                .fetchJoin()
                .innerJoin(qCouponProduct.product, qProduct)
                .fetchJoin();
        final List<Coupon> couponList = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl<>(couponList, pageable, query.fetchCount());
    }
}
