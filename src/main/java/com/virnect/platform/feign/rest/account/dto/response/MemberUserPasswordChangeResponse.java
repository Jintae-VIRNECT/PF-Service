package com.virnect.platform.feign.rest.account.dto.response;

import java.time.LocalDateTime;

import org.thymeleaf.util.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class MemberUserPasswordChangeResponse {
	public static final MemberUserPasswordChangeResponse EMPTY = new MemberUserPasswordChangeResponse(false, "", "", LocalDateTime.now());
	@ApiModelProperty(value = "비밀번호 변경 여부", notes = "true 면 성공적으로 비밀번호가 변경된것입니다.", example = "true")
	private boolean isChanged;
	@ApiModelProperty(value = "비밀번호가 변경된 이메일 정보", position = 1, notes = "새 비밀번호가 설정된 계정의 이메일 정보입니다.", example = "test1234")
	private String email;
	@ApiModelProperty(value = "비밀번호가 변경된 이메일 정보", position = 2, notes = "새 비밀번호가 설정된 계정의 식별자 정보입니다.", example = "asdnlkx9123k90sas")
	private String uuid;
	@ApiModelProperty(value = "비밀번호가 새로 설정된 날짜입니다.", position = 3, notes = "비밀번호가 변경된 날짜 정보입니다.", example = "2020-01-20T14:05:30")
	private LocalDateTime passwordChangedDate;

	public MemberUserPasswordChangeResponse(
		boolean isChanged, String email, String uuid, LocalDateTime passwordChangedDate
	) {
		this.isChanged = isChanged;
		this.email = email;
		this.uuid = uuid;
		this.passwordChangedDate = passwordChangedDate;
	}

	@Override
	public String toString() {
		return "MemberUserPasswordChangeResponse{" +
			"isChanged=" + isChanged +
			", email='" + email + '\'' +
			", uuid='" + uuid + '\'' +
			", passwordChangedDate=" + passwordChangedDate +
			'}';
	}

	public boolean isEmtpy() {
		return StringUtils.isEmpty(uuid);
	}
}
