package com.virnect.workspace.application.license;

import java.util.List;

import com.virnect.workspace.application.license.dto.LicenseRevokeResponse;
import com.virnect.workspace.application.license.dto.MyLicenseInfoListResponse;
import com.virnect.workspace.application.license.dto.MyLicenseInfoResponse;
import com.virnect.workspace.application.license.dto.UserLicenseInfoResponse;
import com.virnect.workspace.application.license.dto.WorkspaceLicensePlanInfoResponse;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-23
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@FeignClient(name = "license-server", fallbackFactory = LicenseRestFallbackFactory.class, configuration = FeignConfiguration.class)
public interface LicenseRestService {
    @GetMapping(value = "/licenses/{workspaceId}/plan")
    ApiResponse<WorkspaceLicensePlanInfoResponse> getWorkspaceLicenses(@PathVariable("workspaceId") String workspaceId);

    @GetMapping(value = "/licenses/{workspaceId}/{userId}")
    ApiResponse<MyLicenseInfoListResponse> getMyLicenseInfoRequestHandler(@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId);

    @PutMapping(value = "/licenses/{workspaceId}/{userId}/grant")
    ApiResponse<MyLicenseInfoResponse> grantWorkspaceLicenseToUser(@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId, @RequestParam(value = "productName") String productName);

    @PutMapping(value = "/licenses/{workspaceId}/{userId}/revoke")
    ApiResponse<LicenseRevokeResponse> revokeWorkspaceLicenseToUser(@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId, @RequestParam(value = "productName") String productName);

    // 워크스페이스 라이선스 전체 조회용.
    @GetMapping(value = "/licenses/{workspaceId}")
    ApiResponse<UserLicenseInfoResponse> getUserLicenseInfoList(
        @PathVariable("workspaceId") String workspaceId, @RequestParam("userIds") List<String> workspaceUserIdList,
        @RequestParam("product") String productName
    );
}
