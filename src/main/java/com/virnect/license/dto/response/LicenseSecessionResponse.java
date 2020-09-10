package com.virnect.license.dto.response;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ApiModel
public class LicenseSecessionResponse {
	@ApiModelProperty(value = "삭제 된 워크스페이스 고유 식별자", example = "12312983asks9d1j")
	private final String workspaceUUID;
	@ApiModelProperty(value = "워크스페이스 정보 삭제 처리 결과", position = 1, example = "true")
	private final boolean result;
	@ApiModelProperty(value = "삭제 처리 일자", position = 2, example = "2020-01-20T14:05:30")
	private final LocalDateTime deletedDate;

	@Override
	public String toString() {
		return "WorkspaceSecessionResponse{" +
			"workspaceUUID='" + workspaceUUID + '\'' +
			", result=" + result +
			", deletedDate=" + deletedDate +
			'}';
	}
}
