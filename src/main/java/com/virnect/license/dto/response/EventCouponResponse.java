package com.virnect.license.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@ApiModel
@Getter
@Setter
@RequiredArgsConstructor
public class EventCouponResponse {
    @ApiModelProperty(value = "쿠폰 발급 처리 결과", notes = "true 면 성공적으로 발급됨, 쿠폰 발급을 요청한 사용자의 이메일로 시리얼 코드 전송됨", example = "true")
    private final boolean couponGenerateResult;
    @ApiModelProperty(value = "쿠폰 시리얼 코드", notes = "쿠폰 시리얼 코드입니다.", example = "1831AD0B-9056-4C85-9BBA-36F267D8EAD1")
    private final String serialKey;
    @ApiModelProperty(value = "쿠폰 발급 일자", notes = "쿠폰이 발급된 일자", example = "true")
    private final LocalDateTime couponRegisterDate;
}
