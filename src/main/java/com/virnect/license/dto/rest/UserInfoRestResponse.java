package com.virnect.license.dto.rest;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class UserInfoRestResponse {
    @ApiModelProperty(value = "사용자 식별자", example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String uuid;
    @ApiModelProperty(value = "사용자 이메일", position = 1, example = "smic1")
    private String email;
    @ApiModelProperty(value = "사용자 이름", position = 2, example = "SMIC Master")
    private String name;
    @ApiModelProperty(value = "사용자 소개", position = 3, example = "smic 워크스페이스 유저")
    private String description;
    @ApiModelProperty(value = "사용자 프로필 이미지 URL", position = 4, example = "smic 워크스페이스 유저")
    private String profile;
    @ApiModelProperty(value = "계정 잠금 여부 ( ACTIVE(잠금), INACTIVE(해제) )", position = 5, example = "smic 워크스페이스 유저")
    private String loginLock;
    @ApiModelProperty(value = "사용자 타입 ( USER(일반), SUB_USER(서브유저), VIRNECT_USER(사내 유저) )", position = 6, example = "smic 워크스페이스 유저")
    private String userType;
    @ApiModelProperty(value = "계정 생성 일자", position = 7, example = "2020-01-20T14:05:30")
    private LocalDateTime createdDate;
    @ApiModelProperty(value = "최종 계정 정보 수정 일자", position = 8, example = "2020-01-20T14:05:30")
    private LocalDateTime updatedDate;

    @Override
    public String toString() {
        return "UserInfoResponseDto{" +
                "uuid='" + uuid + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", profile='" + profile + '\'' +
                ", loginLock=" + loginLock +
                ", userType=" + userType +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
