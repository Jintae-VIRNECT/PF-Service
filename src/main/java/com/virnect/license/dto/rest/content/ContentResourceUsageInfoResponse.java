package com.virnect.license.dto.rest.content;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@RequiredArgsConstructor
public class ContentResourceUsageInfoResponse {
	@ApiModelProperty(value = "워크스페이스 식별자", example = "4d6eab0860969a50acbfa4599fbb5ae8")
	private final String workspaceId;
	@ApiModelProperty(value = "현재 해당 워크스페이스가 사용한 (컨텐츠 + 프로젝트) 스토리지 용량(MB)", position = 1, example = "100")
	private final long storageUsage;
	@ApiModelProperty(value = "현재 해당 워크스페이스의 컨텐츠 (컨텐츠 + 프로젝트) 다운로드 총 횟수", position = 2, example = "915")
	private final long totalHit;
	@ApiModelProperty(value = "현재 해당 워크스페이스가 사용한 컨텐츠 스토리지 용량(MB)", position = 3, example = "100")
	private final long contentStorageUsage;
	@ApiModelProperty(value = "현재 해당 워크스페이스의 컨텐츠 컨텐츠 다운로드 총 횟수", position = 4, example = "915")
	private final long contentTotalHit;
	@ApiModelProperty(value = "현재 해당 워크스페이스가 사용한 프로젝트 스토리지 용량(MB)", position = 5, example = "100")
	private final long projectStorageUsage;
	@ApiModelProperty(value = "현재 해당 워크스페이스의 컨텐츠 프로젝트 다운로드 총 횟수", position = 6, example = "915")
	private final long projectTotalHit;
	@ApiModelProperty(value = "워크스페이스 사용량 통계 일자", position = 7, example = "2020-07-07T17:14:45.374")
	private final LocalDateTime usageReportDate;

	@Override
	public String toString() {
		return "ContentResourceUsageInfoResponse{" +
				"workspaceId='" + workspaceId + '\'' +
				", storageUsage=" + storageUsage +
				", totalHit=" + totalHit +
				", contentStorageUsage=" + contentStorageUsage +
				", contentTotalHit=" + contentTotalHit +
				", projectStorageUsage=" + projectStorageUsage +
				", projectTotalHit=" + projectTotalHit +
				", usageReportDate=" + usageReportDate +
				'}';
	}
}
