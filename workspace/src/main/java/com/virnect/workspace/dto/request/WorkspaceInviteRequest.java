package com.virnect.workspace.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.workspace.domain.workspace.Role;

/**
 * Project: PF-Workspace
 * DATE: 2020-02-19
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceInviteRequest {
	@ApiModelProperty(value = "userId", example = "498b1839dc29ed7bb2ee90ad6985c608")
	@NotBlank(message = "초대하는 유저의 UUID는 필수 값입니다.")
	private String userId;

	@Valid
	private List<UserInfo> userInfoList;

	@ApiModelProperty(hidden = true)
	public boolean existMasterUserInvite() {
		return userInfoList.stream().anyMatch(userInfo -> userInfo.getRole().equals(Role.MASTER.name()));
	}

	@Override
	public String toString() {
		return "WorkspaceInviteRequest{" +
			"userId='" + userId + '\'' +
			", userInfoList=" + userInfoList +
			'}';
	}

	@Getter
	@Setter
	public static class UserInfo {
		@ApiModelProperty(value = "email", example = "ljk@virnect.com", position = 0)
		@NotBlank(message = "초대할 유저의 이메일 주소는 필수값입니다.")
		@Email
		private String email;
		@ApiModelProperty(value = "role", example = "MEMBER", position = 1)
		@NotBlank(message = "초대할 유저의 워크스페이스 권한은 필수값입니다.")
		private String role;
		@ApiModelProperty(value = "planRemote", position = 2)
		@NotNull(message = "초대할 유저의 리모트 플랜 부여 여부는 필수값입니다.")
		private boolean planRemote;
		@ApiModelProperty(value = "planMake", position = 3)
		@NotNull(message = "초대할 유저의 메이크 플랜 부여 여부는 필수값입니다.")
		private boolean planMake;
		@ApiModelProperty(value = "planView", position = 4)
		@NotNull(message = "초대할 유저의 뷰 플랜 부여 여부는 필수값입니다.")
		private boolean planView;

		@Override
		public String toString() {
			return "UserInfo{" +
				"email='" + email + '\'' +
				", role='" + role + '\'' +
				", planRemote=" + planRemote +
				", planMake=" + planMake +
				", planView=" + planView +
				'}';
		}
	}
}

