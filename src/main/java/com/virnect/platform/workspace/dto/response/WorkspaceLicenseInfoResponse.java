package com.virnect.platform.workspace.dto.response;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-06-22
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@ApiModel
public class WorkspaceLicenseInfoResponse {
	@ApiModelProperty(value = "플랜 제품별 정보 라이선스 정보", position = 0)
	private List<LicenseInfo> licenseInfoList = new ArrayList<>();
	@ApiModelProperty(value = "AR 스토리지 용량(GB)", position = 1, example = "10")
	private long maxStorageSize = 0L;
	@ApiModelProperty(value = "온라인 AR 콘텐츠 조회 수", position = 2, example = "100000")
	private long maxDownloadHit = 0L;
	@ApiModelProperty(value = "통화 시간(Hour)", position = 3, example = "10")
	private long maxCallTime = 0L;
    /*@ApiModelProperty(value = "추가된 AR 스토리지 용량", position = 0, example = "")
    private long addMaxStorageSize = 0L;
    @ApiModelProperty(value = "추가된 온라인 AR 콘텐츠 조회 수", position = 0, example = "")
    private long addMaxDownloadHit= 0L;
    @ApiModelProperty(value = "추가된 통화 시간", position = 0, example = "")
    private long addMaxCallTime= 0L;
    @ApiModelProperty(value = "다음 결제일", position = 0, example = "")
    private LocalDateTime nextPaymentDate;
    @ApiModelProperty(value = "결제 수단", position = 0, example = "신용카드")
    private String paymentType = "";*/

	@Getter
	@Setter
	public static class LicenseInfo {
		@ApiModelProperty(value = "제품명", position = 0, example = "REMOTE")
		private String productName;
		@ApiModelProperty(value = "제품 라이선스 타입", position = 1, example = "BASIC")
		private String licenseType;
		@ApiModelProperty(value = "사용된 제품 라이선스 개수", position = 2, example = "4")
		private int useLicenseAmount;
		@ApiModelProperty(value = "전체 제품 라이선스 개수", position = 3, example = "6")
		private int LicenseAmount;
	}
}
