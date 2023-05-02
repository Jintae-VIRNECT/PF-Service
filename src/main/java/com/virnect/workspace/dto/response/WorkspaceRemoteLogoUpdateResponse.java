package com.virnect.workspace.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class WorkspaceRemoteLogoUpdateResponse {
	@ApiModelProperty(value = "로고 변경 결과", example = "true", position = 0)
	private final boolean result;

	@ApiModelProperty(value = "기본 로고 이미지 URL", example = "", position = 1)
	private final String remoteAndroidSplashLogo;

	@ApiModelProperty(value = "그레이 로고 이미지 URL", example = "", position = 2)
	private final String remoteAndroidLoginLogo;

	@ApiModelProperty(value = "화이트 로고 이미지 URL", example = "", position = 3)
	private final String remoteHololens2CommonLogo;
}
