package com.virnect.license.dto.license.request;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLicenseInfoRetrieveRequest {
	@NotBlank
	@ApiModelProperty(value = "워크스페이스 식별자 정보", required = true)
	private String workspaceId;
	@ApiModelProperty(value = "사용자 식별자 배열", position = 1, required = true)
	@NotNull
	private List<String> userIds = new ArrayList<>();
	@ApiModelProperty(value = "라이선스 조회 제품명 (필터링용)", position = 2)
	private String product;

	@Override
	public String toString() {
		return "UserLicenseInfoRetrieveRequest{" +
			"workspaceId='" + workspaceId + '\'' +
			", userIds=" + userIds +
			'}';
	}
}
