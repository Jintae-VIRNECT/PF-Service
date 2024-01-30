package com.virnect.platform.license.dto.license.response;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.platform.license.domain.product.LicenseProductStatus;

@Getter
@Setter
@ApiModel
public class MyLicensePlanInfoResponse {
	@ApiModelProperty(value = "플랜 이용 상품 명", example = "Remote")
	private String planProduct;
	@ApiModelProperty(value = "워크스페이스 고유 식별자", position = 1, example = "4d6eab0860969a50acbfa4599fbb5ae8")
	private String workspaceId;
	@ApiModelProperty(value = "워크스페이스 이름", position = 2, example = "1일 1깡 워크스페이스")
	private String workspaceName;
	@ApiModelProperty(value = "워크스페이스 프로필 이미지", position = 3, example = "http://192.168.6.3:8082/workspaces/upload/issue-sample.jpg")
	private String workspaceProfile;
	@ApiModelProperty(value = "플랜 갱신 일자", position = 4, example = "2020-03-31T09:21:35")
	private LocalDateTime renewalDate;
	@ApiModelProperty(value = "플랜 상태 정보(ACTIVE or EXCEEDED)", position = 5, example = "ACTIVE")
	private LicenseProductStatus productPlanStatus;

	@Override
	public String toString() {
		return "MyLicensePlanInfoResponse{" +
			"planProduct='" + planProduct + '\'' +
			", workspaceId='" + workspaceId + '\'' +
			", workspaceName='" + workspaceName + '\'' +
			", workspaceProfile='" + workspaceProfile + '\'' +
			", renewalDate=" + renewalDate +
			", productPlanStatus=" + productPlanStatus +
			'}';
	}
}
