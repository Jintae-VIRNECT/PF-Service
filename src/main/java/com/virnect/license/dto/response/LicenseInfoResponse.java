package com.virnect.license.dto.response;

import com.virnect.license.domain.license.LicenseStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel
public class LicenseInfoResponse {
    @ApiModelProperty(value = "라이선스 키", example = "29923633-47D7-439C-B515-94FF4A9B5BB0")
    private String licenseKey;
    @ApiModelProperty(value = "할당된 유저 식별자", position = 1, example = "")
    private String userId;
    @ApiModelProperty(value = "라이선스 상태", position = 2, example = "UNUSE")
    private LicenseStatus status;
    @ApiModelProperty(value = "라이선스 생성일", position = 3, example = "2020-04-16T16:34:35")
    private LocalDateTime createdDate;
    @ApiModelProperty(value = "라이선스 생성일", position = 4, example = "2020-04-16T16:34:35")
    private LocalDateTime updatedDate;
}
