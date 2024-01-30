package com.virnect.platform.feign.rest.account.dto.response;

import java.time.LocalDateTime;

import org.thymeleaf.util.StringUtils;

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
public class UserDeleteRestResponse {
	public static final UserDeleteRestResponse EMPTY = new UserDeleteRestResponse();
	@ApiModelProperty(value = "삭제된 사용자의 식별자 정보", example = "asdaskjdhaksdasd")
	private String userUUID;
	@ApiModelProperty(value = "삭제된 날짜정보", position = 1, example = "2020-01-20T14:05:30")
	private LocalDateTime deletedDate;

	@Override
	public String toString() {
		return "UserDeleteResponse{" +
			"userUUID='" + userUUID + '\'' +
			", deletedDate=" + deletedDate +
			'}';
	}

	public boolean isEmpty() {
		return StringUtils.isEmpty(userUUID);
	}
}
