package com.virnect.license.global.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
	@ApiModelProperty(value = "API 응답 데이터를 갖고 있는 객체.", dataType = "object")
	T data;
	@ApiModelProperty(value = "API 처리 결과 상태 코드 값, 200이면 정상 처리 완료.", dataType = "int", example = "200")
	int code = 200;
	@ApiModelProperty(value = "API 처리 결과에 대한 메시지", dataType = "string", example = "success")
	String message = "complete";

	public ApiResponse(T data) {
		this.data = data;
	}
}
