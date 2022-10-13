package com.virnect.workspace.application.license;

import com.virnect.workspace.application.license.dto.LicenseRevokeResponse;
import com.virnect.workspace.application.license.dto.MyLicenseInfoListResponse;
import com.virnect.workspace.application.license.dto.MyLicenseInfoResponse;
import com.virnect.workspace.application.license.dto.WorkspaceLicensePlanInfoResponse;
import com.virnect.workspace.global.common.ApiResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-29
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Component
public class LicenseRestFallbackFactory implements FallbackFactory<LicenseRestService> {
    @Override
    public LicenseRestService create(Throwable cause) {
        return new LicenseRestService() {
            @Override
            public ApiResponse<WorkspaceLicensePlanInfoResponse> getWorkspaceLicenses(String workspaceId) {
                return new ApiResponse<>(new WorkspaceLicensePlanInfoResponse());
            }

            @Override
            public ApiResponse<MyLicenseInfoListResponse> getMyLicenseInfoRequestHandler(String userId, String workspaceId) {
                return new ApiResponse<>(new MyLicenseInfoListResponse());
            }

            @Override
            public ApiResponse<MyLicenseInfoResponse> grantWorkspaceLicenseToUser(String workspaceId, String userId, String productName) {
                return new ApiResponse<>(new MyLicenseInfoResponse());
            }

            @Override
            public ApiResponse<LicenseRevokeResponse> revokeWorkspaceLicenseToUser(String workspaceId, String userId, String productName) {
                return new ApiResponse<>(new LicenseRevokeResponse());
            }
        };
    }
}
