package com.virnect.workspace.application.user.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Project: PF-Workspace
 * DATE: 2020-11-18
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class InviteUserInfoResponse {
	@ApiModelProperty(value = "회원 유저 여부")
	private boolean isMemberUser;
	@ApiModelProperty(value = "초대될 사용자들의 정보가 담긴 배열")
	private InviteUserDetailInfoResponse inviteUserDetailInfo;
}
