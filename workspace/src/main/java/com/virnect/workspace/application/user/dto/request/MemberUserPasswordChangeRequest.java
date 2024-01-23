package com.virnect.workspace.application.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@ApiModel
@RequiredArgsConstructor
public class MemberUserPasswordChangeRequest {
	@ApiModelProperty(value = "새 비밀번호가 설정될 멤버 사용자의 식별자 정보입니다.", example = "498b1839dc29ed7bb2ee90ad6985c608")
	private final String uuid;
	@ApiModelProperty(value = "새 비밀번호", notes = "새로 설정할 비밀번호르 입력합니다.", position = 1, example = "test123456")
	private final String password;

	@Override
	public String toString() {
		return "UserPasswordChangeRequest{" +
			"uuid='" + uuid + '\'' +
			", password='" + "*********" + '\'' +
			'}';
	}
}
