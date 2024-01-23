package com.virnect.workspace.application.user.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-14
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
@ApiModel
public class UserInfoAccessCheckResponse {
	@ApiModelProperty(value = "접근 권한 확인 결과", notes = "(true : 허용, false: 비허용)", example = "true")
	private boolean accessCheckResult;
	@ApiModelProperty(value = "사용자 정보", notes = "accessCheckResult 가 true 인 경우에만 반환됨", position = 1)
	private UserInfoRestResponse userInfo;
}
