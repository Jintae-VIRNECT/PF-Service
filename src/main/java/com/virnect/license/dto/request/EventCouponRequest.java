package com.virnect.license.dto.request;

import com.virnect.license.domain.Status;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
@Getter
@Setter
@ApiModel
public class EventCouponRequest {
    @NotBlank
    @ApiModelProperty(value = "사용자 식별번호", example = "498b1839dc29ed7bb2ee90ad6985c608")
    private String userId;
    @NotNull
    @ApiModelProperty(value = "선택된 제품 이름", notes = "VIEW 는 제외함", dataType = "String[]", position = 1, example = "[\"REMOTE\",\"MAKE\"]")
    private String[] products;
    @NotBlank
    @ApiModelProperty(value = "회사 이름", position = 2, example = "VIRENCT")
    private String companyName;
    @NotBlank
    @ApiModelProperty(value = "부서명", position = 3, example = "플랫폼팀")
    private String department;
    @NotBlank
    @ApiModelProperty(value = "직책", position = 4, example = "서버개발")
    private String position;
    @NotBlank
    @ApiModelProperty(value = "회사 이메일", position = 5, example = "sky456139@virnect.com")
    private String companyEmail;
    @NotBlank
    @ApiModelProperty(value = "연락처", position = 6, example = "010-1234-1234")
    private String callNumber;
    @NotBlank
    @ApiModelProperty(value = "회사 웹사이트", position = 7, example = "https://virnect.com")
    private String companySite;
    @NotBlank
    @ApiModelProperty(value = "서비스 분야", position = 9, example = "IT 서비스")
    private String companyService;
    @NotBlank
    @ApiModelProperty(value = "직원 수", position = 10, example = "50~100명")
    private String companyWorker;
    @NotBlank
    @Length(max = 500)
    @ApiModelProperty(value = "제품 사용 이유 또는 의견", position = 11, example = "킹왕짱멋져서")
    private String content;
    @NotNull
    @ApiModelProperty(value = "마케팅 활용 동의", position = 12, example = "ACCEPT")
    private Status marketInfoReceivePolicy;

    @Override
    public String toString() {
        return "EventCouponRequest{" +
                "userId='" + userId + '\'' +
                ", products=" + Arrays.toString(products) +
                ", companyName='" + companyName + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", companyEmail='" + companyEmail + '\'' +
                ", callNumber='" + callNumber + '\'' +
                ", companySite='" + companySite + '\'' +
                ", companyService='" + companyService + '\'' +
                ", companyWorker=" + companyWorker +
                ", content='" + content + '\'' +
                ", marketInfoReceivePolicy=" + marketInfoReceivePolicy +
                '}';
    }
}
