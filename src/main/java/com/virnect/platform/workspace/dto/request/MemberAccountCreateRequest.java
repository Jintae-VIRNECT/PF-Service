package com.virnect.platform.workspace.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import com.virnect.platform.workspace.domain.workspace.Role;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class MemberAccountCreateRequest {
	@ApiModelProperty(value = "계정 생성 요청 유저 식별자", required = true, example = "498b1839dc29ed7bb2ee90ad6985c608", position = 0)
	@NotBlank
	private String userId;
	@Valid
	private List<MemberAccountCreateInfo> memberAccountCreateRequest;

	//게스트 권한이 부여된 전용 계정을 생성 할 수 없음
	public boolean existGuestRoleUser() {
		return memberAccountCreateRequest.stream().anyMatch(memberAccountCreateInfo -> memberAccountCreateInfo.getRole().equals(Role.GUEST.name()));
	}

	//마스터 권한이 부여된 전용 계정을 생성할 수 없음.
	public boolean existMasterRoleUser() {
		return memberAccountCreateRequest.stream().anyMatch(memberAccountCreateInfo -> memberAccountCreateInfo.getRole().equals(Role.MASTER.name()));
	}

	@Override
	public String toString() {
		return "MemberAccountCreateRequest{" +
			"userId='" + userId + '\'' +
			", memberAccountCreateRequest=" + memberAccountCreateRequest +
			'}';
	}
}
