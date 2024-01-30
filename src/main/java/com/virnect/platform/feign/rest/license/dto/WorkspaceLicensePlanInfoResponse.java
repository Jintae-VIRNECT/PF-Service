package com.virnect.platform.feign.rest.license.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.platform.workspace.domain.rest.LicenseProductStatus;
import com.virnect.platform.workspace.domain.rest.LicenseStatus;
import com.virnect.platform.workspace.domain.rest.PlanStatus;

/**
 * Project: PF-Workspace
 * DATE: 2020-05-27
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@ApiModel
public class WorkspaceLicensePlanInfoResponse {
	@ApiModelProperty(value = "워크스페이스 식별자", example = "4d6eab0860969a50acbfa4599fbb5ae8")
	private String workspaceId = "";
	@ApiModelProperty(value = "라이선스 플랜 마스터 사용자 식별자", position = 1, example = "498b1839dc29ed7bb2ee90ad6985c608")
	private String masterUserUUID = "";
	@ApiModelProperty(value = "워크스페이스 이용 최대 사용자 수 ", notes = "마스터 계정 포함", position = 2, example = "50")
	private Long maxUserAmount = 0L;
	@ApiModelProperty(value = "워크스페이스 최대 저장 용량(byte 단위)", notes = "byte 단위의 최대 용량", position = 3, example = "9663676416")
	private Long maxStorageSize = 0L;
	@ApiModelProperty(value = "워크스페이스 최대 컨텐츠 다운로드 수", position = 4, example = "5000")
	private Long maxDownloadHit = 0L;
	@ApiModelProperty(value = "워크스페이스 최대 통화 횟수", position = 5, example = "100")
	private Long maxCallTime = 0L;
	@ApiModelProperty(value = "라이선스 플랜 시작일자", position = 6, example = "2020-05-15T13:00:00")
	private LocalDateTime startDate = LocalDateTime.now();
	@ApiModelProperty(value = "라이선스 플랜 종료일자", position = 7, example = "2020-05-15T13:00:00")
	private LocalDateTime endDate = LocalDateTime.now();
	@ApiModelProperty(value = "라이선스 플랜 상태", position = 8, example = "ACTIVE")
	private PlanStatus planStatus;
	@ApiModelProperty(value = "플랜 제품별 정보 라이선스 정보", position = 9)
	private List<LicenseProductInfoResponse> licenseProductInfoList = Collections.emptyList();

	@Getter
	@Setter
	@ApiModel
	public static class LicenseProductInfoResponse {
		@ApiModelProperty(value = "제품 식별자", example = "1")
		private long productId;
		@ApiModelProperty(value = "제품명", position = 1, example = "MAKE")
		private String productName;
		@ApiModelProperty(value = "제품 라이선스 타입", position = 2, example = "BASIC")
		private String licenseType;
		@ApiModelProperty(value = "재품 라이선스 수량", position = 3, example = "4")
		private int quantity;
		@ApiModelProperty(value = "현재 할당 가능한 라이선스 수량", position = 4, example = "1")
		private int unUseLicenseAmount;
		@ApiModelProperty(value = "현재 할당 된 라이선스 수량", position = 5, example = "1")
		private int useLicenseAmount;
		@ApiModelProperty(value = "제품 라이선스 플랜 상태(ACTIVE,INACTIVE,EXCEEDED)", position = 6, example = "ACTIVE")
		private LicenseProductStatus productStatus;
		@ApiModelProperty(value = "라이선스 정보", position = 6)
		private List<LicenseInfoResponse> licenseInfoList;
	}

	@Getter
	@Setter
	@ApiModel
	public static class LicenseInfoResponse {
		@ApiModelProperty(value = "라이선스 키", example = "29923633-47D7-439C-B515-94FF4A9B5BB0")
		private String licenseKey;
		@ApiModelProperty(value = "할당된 유저 식별자", position = 1, example = "")
		private String userId;
		@ApiModelProperty(value = "라이선스 상태", position = 2, example = "UNUSE")
		private LicenseStatus status;
		@ApiModelProperty(value = "라이선스 생성일", position = 3, example = "2020-04-16T16:34:35")
		private LocalDateTime createdDate;
		@ApiModelProperty(value = "라이선스 생성일", position = 4, example = "2020-04-16T16:34:35")
		private LocalDateTime updatedDate;
	}
}
