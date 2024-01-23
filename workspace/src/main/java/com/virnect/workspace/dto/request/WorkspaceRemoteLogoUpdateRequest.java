package com.virnect.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.workspace.global.validator.AllowedExtension;

@Getter
@Setter
@ApiModel
public class WorkspaceRemoteLogoUpdateRequest {
	@ApiModelProperty(value = "로고 변경 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608", position = 0)
	@NotBlank
	private String userId;

	@AllowedExtension(extensions = {AllowedExtension.Extension.PNG})
	@ApiModelProperty(value = "안드로이드 스플래시 로고 이미지", required = false, position = 1)
	private MultipartFile remoteAndroidSplashLogo;

	@ApiModelProperty(value = "안드로이드 스플래시 로고 기본 이미지로 변경", required = false, position = 2)
	private boolean defaultRemoteAndroidSplashLogo = false;

	@AllowedExtension(extensions = {AllowedExtension.Extension.PNG})
	@ApiModelProperty(value = "안드로이드 로그인 로고 이미지", required = false, position = 3)
	private MultipartFile remoteAndroidLoginLogo;

	@ApiModelProperty(value = "안드로이드 로그인 로고 기본 이미지로 변경", required = false, position = 4)
	private boolean defaultRemoteAndroidLoginLogo = false;

	@AllowedExtension(extensions = {AllowedExtension.Extension.PNG})
	@ApiModelProperty(value = "홀로렌즈2 로고 이미지", required = false, position = 5)
	private MultipartFile remoteHololens2CommonLogo;

	@ApiModelProperty(value = "홀로렌즈2 로고 기본 이미지로 변경", required = false, position = 6)
	private boolean defaultRemoteHololens2CommonLogo = false;

	@ApiModelProperty(hidden = true)
	public boolean isUpdateAndroidSplashLogo() {
		return defaultRemoteAndroidSplashLogo || remoteAndroidSplashLogo != null;
	}

	@ApiModelProperty(hidden = true)
	public boolean isUpdateAndroidLoginLogo() {
		return defaultRemoteAndroidLoginLogo || remoteAndroidLoginLogo != null;
	}

	@ApiModelProperty(hidden = true)
	public boolean isUpdateHololens2Logo() {
		return defaultRemoteHololens2CommonLogo || remoteHololens2CommonLogo != null;
	}

	@Override
	public String toString() {
		return "WorkspaceRemoteLogoUpdateRequest{" +
			"userId='" + userId + '\'' +
			", defaultAndroidSplashLogo=" + defaultRemoteAndroidSplashLogo +
			", defaultAndroidLoginLogo=" + defaultRemoteAndroidLoginLogo +
			", defaultHololens2CommonLogo=" + defaultRemoteHololens2CommonLogo +
			'}';
	}
}
