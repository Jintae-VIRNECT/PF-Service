package com.virnect.platform.feign.rest.account.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Project: PF-Workspace
 * DATE: 2021-08-10
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@RequiredArgsConstructor
public class UserInfoModifyRequest {
	@ApiModelProperty(value = "변경할 닉네임", position = 2, notes = "변경할 경우 입력하면됩니다.", example = "닉넴")
	private final String nickname;
}
