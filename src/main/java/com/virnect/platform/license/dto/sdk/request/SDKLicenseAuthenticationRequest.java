package com.virnect.platform.license.dto.sdk.request;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SDKLicenseAuthenticationRequest {
	@NotBlank
	private String serialKey;

	@Override
	public String toString() {
		return "SDKLicenseAuthenticationRequest{" +
			"serialKey='" + serialKey + '\'' +
			'}';
	}
}
