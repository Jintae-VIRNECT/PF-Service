package com.virnect.license.dto.response.admin;

import com.virnect.license.domain.coupon.CouponPeriodType;
import com.virnect.license.domain.coupon.CouponStatus;
import com.virnect.license.domain.Status;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.05.06
 */

@Getter
@Setter
@ApiModel
public class AdminCouponInfoResponse {
    @ApiModelProperty(value = "쿠폰 식별자", example = "1")
    private long id;
    @ApiModelProperty(value = "쿠폰 이름", position = 1, example = "2주 무료 사용 쿠폰")
    private String name;
    @ApiModelProperty(value = "쿠폰 시리얼 코드", position = 2, example = "1831AD0B-9056-4C85-9BBA-36F267D8EAD1")
    private String serialKey;
    @ApiModelProperty(value = "쿠폰 등록일", position = 3, example = "2020-01-13 07:55:47")
    private LocalDateTime registerDate;
    @ApiModelProperty(value = "쿠폰 만료일", position = 4, example = "2020-01-27 07:55:47")
    private LocalDateTime expiredDate;
    @ApiModelProperty(value = "사용 시작일", position = 5, example = "2020-01-14 07:55:47")
    private LocalDateTime startDate;
    @ApiModelProperty(value = "사용 만료일", position = 6, example = "2020-01-27 07:55:47")
    private LocalDateTime endDate;
    @ApiModelProperty(value = "쿠폰 상태", position = 7, example = "USE")
    private CouponStatus status;
    @ApiModelProperty(value = "쿠폰 사용자 식별번호", position = 8, example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String userId;
    @ApiModelProperty(value = "쿠폰 사용자 회사 정보", position = 9, example = "VIRNECT")
    private String company;
    @ApiModelProperty(value = "쿠폰 사용자 부서 정보", position = 10, example = "플랫폼")
    private String department;
    @ApiModelProperty(value = "쿠폰 사용자 직급/직책 정보", position = 11, example = "서버 개발자")
    private String position;
    @ApiModelProperty(value = "쿠폰 사용자 회사 이메일", position = 12, example = "test@company.com")
    private String companyEmail;
    @ApiModelProperty(value = "쿠폰 사용자 회사 전화 번호", position = 13, example = "02-1234-1234")
    private String callNumber;
    @ApiModelProperty(value = "쿠폰 사용자 회사 홈페이지 주소", position = 13, example = "www.virnect.com")
    private String companySite;
    @ApiModelProperty(value = "쿠폰 사용자 회사 업종", position = 14, example = "IT")
    private String companyCategory;
    @ApiModelProperty(value = "쿠폰 사용자 회사 서비스 분야", position = 15, example = "AR, 스마트팩토리")
    private String companyService;
    @ApiModelProperty(value = "쿠폰 사용자 회사 직원수", position = 16, example = "51~100명")
    private String companyWorker;
    @ApiModelProperty(value = "쿠폰 사용자 제품 사용 이유 또는 의견", position = 17, example = "버넥트 서비스들을 사용해보고 싶습니다.")
    private String content;
    @ApiModelProperty(value = "쿠폰 사용자 개인 정보 제공 동의 여부", position = 18, example = "ACCEPT")
    private Status personalInfoPolicy;
    @ApiModelProperty(value = "쿠폰 사용자 마케팅 활용 동의 여부", position = 19, example = "REJECT")
    private Status marketInfoReceive;
    @ApiModelProperty(value = "쿠폰 라이선스 만료 기간", position = 20, example = "14")
    private long duration;
    @ApiModelProperty(value = "쿠폰 라이선스 만료 기간 타입(일, 주, 월, 연간)", position = 21, example = "WEEK")
    private CouponPeriodType couponPeriodType;
    @ApiModelProperty(value = "쿠폰 라이선스 부여 상품 이름", position = 22, example = "MAKE,VIEW")
    private String[] products;
}
