package com.virnect.license.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class CreateNewProductRequest {
	@NotBlank
	private String productName;
	@NotNull
	@Positive
	private long price;
	@NotNull
	@PositiveOrZero
	private long maxStorageSize;
	@NotNull
	@PositiveOrZero
	private long maxDownloadHit;
	@NotNull
	@PositiveOrZero
	private long maxCallTime;
	@NotNull
	@Positive
	private long productTypeId;
}
