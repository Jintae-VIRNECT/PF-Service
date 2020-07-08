package com.virnect.license.dto.response;

import com.virnect.license.domain.coupon.CouponStatus;
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
 * @since 2020.04.09
 */
@Getter
@Setter
@ApiModel
public class MyCouponInfoResponse {
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

    @Override
    public String toString() {
        return "MyCouponInfoResponse{" +
                "name='" + name + '\'' +
                ", registerDate=" + registerDate +
                ", expiredDate=" + expiredDate +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
