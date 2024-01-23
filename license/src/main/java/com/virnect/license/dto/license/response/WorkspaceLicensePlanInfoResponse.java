package com.virnect.license.dto.license.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.license.domain.licenseplan.PlanStatus;

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
	
	// 라이선스에 따른 사용 가능 리소스 할당 총 정보
	@ApiModelProperty(value = "워크스페이스 이용 최대 사용자 수(마스터 계정 포함 명수)", notes = "마스터 계정 포함", position = 2, example = "9")
	private long maxUserAmount = 0L;
	@ApiModelProperty(value = "워크스페이스 최대 저장 용량(MB 단위)", notes = "byte 단위의 최대 용량", position = 3, example = "9000")
	private long maxStorageSize = 0L;
	@ApiModelProperty(value = "워크스페이스 최대 컨텐츠 다운로드 수(횟수)", position = 4, example = "6000")
	private long maxDownloadHit = 0L;
	@ApiModelProperty(value = "워크스페이스 최대 통화 시간(Hour)", position = 5, example = "90")
	private long maxCallTime = 0L;
	
	// 제품 라이선스 상품에 따른 리소스 할당 정보
	@ApiModelProperty(value = "라이선스 플랜 기본 저장 용량 정보(MB)", notes = "MB 단위의 최대 용량", position = 6, example = "5000")
	private long defaultStorageSize = 0L;
	@ApiModelProperty(value = "라이선스 플랜 기본다운로드 횟수 정보", position = 7, example = "5000")
	private long defaultDownloadHit = 0L;
	@ApiModelProperty(value = "라이선스 플랜 기본 통화 시간 정보(Hour)", position = 8, example = "60")
	private long defaultCallTime = 0L;
	
	// 추가 리소스 이용 상품에 따른 리소스 할당 정보
	@ApiModelProperty(value = "저장 용량 추가 서비스 이용권 정보(MB)", notes = "byte 단위의 최대 용량", position = 9, example = "4000")
	private long addStorageSize = 0L;
	@ApiModelProperty(value = "다운로드 횟수 추가 서비스 이용권 정보", position = 10, example = "1000")
	private long addDownloadHit = 0L;
	@ApiModelProperty(value = "통화 추가 서비스 이용권 정보(Hour)", position = 11, example = "30")
	private long addCallTime = 0L;

	// 총 리소스 정보
	@ApiModelProperty(value = "현재 해당 워크스페이스의 컨텐츠 (컨텐츠 + 프로젝트) 다운로드 총 횟수", position = 12, example = "10")
	private long currentUsageDownloadHit = 0L;
	@ApiModelProperty(value = "현재 해당 워크스페이스의 총 저장 용량 정보 (프로젝트 용량 + 리모트 저장 용량 + 컨텐츠 용량) (MB)", position = 13 , example = "300")
	private long currentUsageStorage = 0L;
	@ApiModelProperty(value = "현재 해당 워크스페이스의 총 통화 사용량 정보 (분 단위)", position = 14, example = "0")
	private long currentUsageCallTime = 0L;

	// 용량 리소스 디테일 정보
	@ApiModelProperty(value = "현재 워크스페이스 리모트 서비스 저장 용량 정보 (MB)", position = 15, example = "100")
	private long currentRemoteServiceStorageUsage = 0L;
	@ApiModelProperty(value = "현재 워크스페이스 컨텐츠 저장 용량 정보 (MB)", position = 16, example = "100")
	private long currentContentStorageUsage = 0L;
	@ApiModelProperty(value = "현재 워크스페이스 프로젝트 저장 용량 정보 (MB)", position = 17, example = "100")
	private long currentProjectStorageUsage = 0L;

	// 다운로드 횟수 디테일 정보
	@ApiModelProperty(value = "현재 워크스페이스 컨텐츠 다운로드 횟수", position = 18, example = "5")
	private long currentContentDownloadHit = 0L;
	@ApiModelProperty(value = "현재 워크스페이스 프로젝트 다운로드 횟수", position = 19, example = "5")
	private long currentProjectDownloadHit = 0L;
	
	// 라이선스 플랜 정보
	@ApiModelProperty(value = "라이선스 플랜 시작일자", position = 20, example = "2020-05-15T13:00:00")
	private LocalDateTime startDate = LocalDateTime.now();
	@ApiModelProperty(value = "라이선스 플랜 종료일자", position = 21, example = "2020-05-15T13:00:00")
	private LocalDateTime endDate = LocalDateTime.now();
	@ApiModelProperty(value = "라이선스 플랜 상태", position = 22, example = "ACTIVE")
	private PlanStatus planStatus = PlanStatus.INACTIVE;
	
	@ApiModelProperty(value = "플랜 제품별 정보 라이선스 정보", position = 23)
	private List<LicenseProductInfoResponse> licenseProductInfoList = new ArrayList<>();


	@Override
	public String toString() {
		return "WorkspaceLicensePlanInfoResponse{" +
				"workspaceId='" + workspaceId + '\'' +
				", masterUserUUID='" + masterUserUUID + '\'' +
				", maxUserAmount=" + maxUserAmount +
				", maxStorageSize=" + maxStorageSize +
				", maxDownloadHit=" + maxDownloadHit +
				", maxCallTime=" + maxCallTime +
				", defaultStorageSize=" + defaultStorageSize +
				", defaultDownloadHit=" + defaultDownloadHit +
				", defaultCallTime=" + defaultCallTime +
				", addStorageSize=" + addStorageSize +
				", addDownloadHit=" + addDownloadHit +
				", addCallTime=" + addCallTime +
				", currentUsageDownloadHit=" + currentUsageDownloadHit +
				", currentUsageStorage=" + currentUsageStorage +
				", currentUsageCallTime=" + currentUsageCallTime +
				", currentRemoteServiceStorageUsage=" + currentRemoteServiceStorageUsage +
				", currentContentStorageUsage=" + currentContentStorageUsage +
				", currentProjectStorageUsage=" + currentProjectStorageUsage +
				", currentContentDownloadHit=" + currentContentDownloadHit +
				", currentProjectDownloadHit=" + currentProjectDownloadHit +
				", startDate=" + startDate +
				", endDate=" + endDate +
				", planStatus=" + planStatus +
				", licenseProductInfoList=" + licenseProductInfoList +
				'}';
	}
}
