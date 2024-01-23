package com.virnect.workspace.dto.request;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-10
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MemberProfileUpdateRequest {
	@ApiModelProperty(value = "변경 요청 유저의 uuid", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608")
	@NotNull
	private String requestUserId;

	@ApiModelProperty(value = "유저 uuid", required = true, example = "4ea61b4ad1dab12fb2ce8a14b02b7460")
	@NotNull
	private String userId;

	@ApiModelProperty(value = "유저 프로필 이미지", required = true)
	private MultipartFile profile;

	@ApiModelProperty(value = "기본이미지로 업데이트 여부", required = false, example = "false")
	private Boolean updateAsDefaultImage;

	@ApiModelProperty(hidden = true)
	public boolean isUserSelfUpdateRequest() {
		return requestUserId.equals(userId);
	}

}
