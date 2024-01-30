package com.virnect.platform.workspace.dto.request;

import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class WorkspaceMemberPasswordChangeRequest {
	@NotBlank
	@ApiModelProperty(value = "멤버의 비밀번호 변경을 요청하는 사용자의 식별자 정보입니다.", example = "498b1839dc29ed7bb2ee90ad6985c608")
	private String requestUserId;
	@NotBlank
	@ApiModelProperty(value = "새 비밀번호가 설정될 멤버 사용자의 식별자 정보입니다.", position = 1, example = "498b1839dc29ed7bb2ee90ad6985c608")
	private String userId;
	@NotBlank
	@ApiModelProperty(value = "새 비밀번호", notes = "새로 설정할 비밀번호르 입력합니다.", position = 2, example = "test123456")
	@Length(min = 8, max = 20)
	private String password;
}
