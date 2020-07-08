package com.virnect.license.dto.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
@RequiredArgsConstructor
public class ContentResourceUsageInfoResponse {
    @ApiModelProperty(value = "워크스페이스 식별자", example = "4d6eab0860969a50acbfa4599fbb5ae8")
    private final String workspaceId;
    @ApiModelProperty(value = "현재 해당 워크스페이스가 사용한 스토리지 용량(MB)", position = 1, example = "100")
    private final long storageUsage;
    @ApiModelProperty(value = "현재 해당 워크스페이스의 컨텐츠 다운로드 총 횟수", position = 2, example = "915")
    private final long totalHit;
    @ApiModelProperty(value = "워크스페이스 사용량 통계 일자", position = 3, example = "2020-07-07T17:14:45.374")
    private final LocalDateTime usageReportDate;


    @Override
    public String toString() {
        return "ContentUsageInfoResponse{" +
                "workspaceId='" + workspaceId + '\'' +
                ", storageUsage=" + storageUsage +
                ", totalHit=" + totalHit +
                ", usageReportDate=" + usageReportDate +
                '}';
    }
}
