package com.virnect.workspace.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-17
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Getter
@Setter
public class WorkspaceUpdateRequest {
    @NotBlank
    private String workspaceId;
    @NotBlank
    private String userId;
    @NotBlank
    @Length(min = 0, max=9, message = "워크스페이스 이름은 최대 9자까지 가능합니다.")
    private String name;

    private MultipartFile profile;

    @NotBlank
    @Length(min = 0, max=39, message = "워크스페이스 설명은 최대 39자까지 가능합니다.")
    private String description;
}
