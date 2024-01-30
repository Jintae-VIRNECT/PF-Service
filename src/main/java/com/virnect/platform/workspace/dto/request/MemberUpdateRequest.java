package com.virnect.platform.workspace.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-03-02
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MemberUpdateRequest {
	@ApiModelProperty(value = "변경 요청 유저의 uuid", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608")
	@NotNull
	private String requestUserId;

	@ApiModelProperty(value = "유저 uuid", required = true, example = "4ea61b4ad1dab12fb2ce8a14b02b7460")
	@NotNull
	private String userId;

	@ApiModelProperty(value = "권한 변경 대상 uuid", required = true, example = "MANAGER")
	private String role;

	@ApiModelProperty(value = "유저의 리모트 플랜 사용 여부", required = false, example = "false")
	private Boolean licenseRemote;

	@ApiModelProperty(value = "유저의 메이크 플랜 사용 여부", required = false, example = "false")
	private Boolean licenseMake;

	@ApiModelProperty(value = "유저의 뷰 플랜 사용 여부", required = false, example = "false")
	private Boolean licenseView;

	@ApiModelProperty(value = "유저의 닉네임", required = false, example = "닉네임")
	private String nickname;

	@ApiModelProperty(hidden = true)
	public boolean isEssentialLicenseToUser() {
		return licenseRemote || licenseMake || licenseView;
	}

	@ApiModelProperty(hidden = true)
	public boolean existLicenseUpdate(List<String> currentLicenseProductList) {
		if (licenseRemote == null && licenseMake == null && licenseView == null) {
			return false;
		}
		return (licenseRemote == null || currentLicenseProductList.contains("REMOTE") != licenseRemote)
			|| (licenseMake == null || currentLicenseProductList.contains("MAKE") != licenseMake)
			|| (licenseView == null || currentLicenseProductList.contains("VIEW") != licenseView);
	}

	@ApiModelProperty(hidden = true)
	public boolean isUserSelfUpdateRequest() {
		return requestUserId.equals(userId);
	}

	@Override
	public String toString() {
		return "MemberUpdateRequest{" +
			"requestUserId='" + requestUserId + '\'' +
			", userId='" + userId + '\'' +
			", role='" + role + '\'' +
			", licenseRemote=" + licenseRemote +
			", licenseMake=" + licenseMake +
			", licenseView=" + licenseView +
			", nickname='" + nickname + '\'' +
			'}';
	}
}
