package com.virnect.license.dto.response;

import com.virnect.license.domain.licenseplan.PlanStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.05.18
 */
@Getter
@Setter
@ApiModel
public class WorkspaceLicensePlanInfoResponse {
    @ApiModelProperty(value = "워크스페이스 식별자", example = "4d6eab0860969a50acbfa4599fbb5ae8")
    private String workspaceId = "";
    @ApiModelProperty(value = "라이선스 플랜 마스터 사용자 식별자", position = 1, example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String masterUserUUID = "";
    @ApiModelProperty(value = "워크스페이스 이용 최대 사용자 수(마스터 계정 포함 명수)", notes = "마스터 계정 포함", position = 2, example = "9")
    private Long maxUserAmount = 0L;
    @ApiModelProperty(value = "워크스페이스 최대 저장 용량(MB 단위)", notes = "byte 단위의 최대 용량", position = 3, example = "9000")
    private Long maxStorageSize = 0L;
    @ApiModelProperty(value = "워크스페이스 최대 컨텐츠 다운로드 수(횟수)", position = 4, example = "6000")
    private Long maxDownloadHit = 0L;
    @ApiModelProperty(value = "워크스페이스 최대 통화 시간(Hour)", position = 5, example = "90")
    private Long maxCallTime = 0L;
    @ApiModelProperty(value = "라이선스 플랜 기본 저장 용량 정보(MB)", notes = "MB 단위의 최대 용량", position = 6, example = "5000")
    private Long defaultStorageSize = 0L;
    @ApiModelProperty(value = "라이선스 플랜 기본다운로드 횟수 정보", position = 7, example = "5000")
    private Long defaultDownloadHit = 0L;
    @ApiModelProperty(value = "라이선스 플랜 기본 통화 시간 정보(Hour)", position = 8, example = "60")
    private Long defaultCallTime = 0L;
    @ApiModelProperty(value = "저장 용량 추가 서비스 이용권 정보(MB)", notes = "byte 단위의 최대 용량", position = 9, example = "4000")
    private Long addStorageSize = 0L;
    @ApiModelProperty(value = "다운로드 횟수 추가 서비스 이용권 정보", position = 10, example = "1000")
    private Long addDownloadHit = 0L;
    @ApiModelProperty(value = "통화 추가 서비스 이용권 정보(Hour)", position = 11, example = "30")
    private Long addCallTime = 0L;
    @ApiModelProperty(value = "워크스페이스 현재 다운로드 횟수 정보", position = 12)
    private Long currentUsageDownloadHit = 0L;
    @ApiModelProperty(value = "워크스페이스 현재 저쟝 용량 정보 (MB)", position = 13)
    private Long currentUsageStorage = 0L;
    @ApiModelProperty(value = "워크스페이스 현재 통화 사용량 정보 (분 단위)", position = 14)
    private Long currentUsageCallTime = 0L;
    @ApiModelProperty(value = "라이선스 플랜 시작일자", position = 15, example = "2020-05-15T13:00:00")
    private LocalDateTime startDate = LocalDateTime.now();
    @ApiModelProperty(value = "라이선스 플랜 종료일자", position = 16, example = "2020-05-15T13:00:00")
    private LocalDateTime endDate = LocalDateTime.now();
    @ApiModelProperty(value = "라이선스 플랜 상태", position = 17, example = "ACTIVE")
    private PlanStatus planStatus = PlanStatus.INACTIVE;
    @ApiModelProperty(value = "플랜 제품별 정보 라이선스 정보", position = 18)
    private List<LicenseProductInfoResponse> licenseProductInfoList = new ArrayList<>();
}
