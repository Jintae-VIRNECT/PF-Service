package com.virnect.platform.feign.rest.license;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.virnect.platform.feign.rest.license.dto.LicenseRevokeResponse;
import com.virnect.platform.feign.rest.license.dto.MyLicenseInfoListResponse;
import com.virnect.platform.feign.rest.license.dto.MyLicenseInfoResponse;
import com.virnect.platform.feign.rest.license.dto.UserLicenseInfoResponse;
import com.virnect.platform.feign.rest.license.dto.WorkspaceLicensePlanInfoResponse;
import com.virnect.platform.global.config.FeignConfiguration;
import com.virnect.platform.workspace.global.common.ApiResponse;

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
	ApiResponse<MyLicenseInfoListResponse> getMyLicenseInfoRequestHandler(
		@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId
	);

	@PutMapping(value = "/licenses/{workspaceId}/{userId}/grant")
	ApiResponse<MyLicenseInfoResponse> grantWorkspaceLicenseToUser(
		@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId,
		@RequestParam(value = "productName") String productName
	);

	@PutMapping(value = "/licenses/{workspaceId}/{userId}/revoke")
	ApiResponse<LicenseRevokeResponse> revokeWorkspaceLicenseToUser(
		@PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId,
		@RequestParam(value = "productName") String productName
	);

	@GetMapping(value = "/licenses/{workspaceId}")
	ApiResponse<UserLicenseInfoResponse> getUserLicenseInfoList(
		@PathVariable("workspaceId") String workspaceId, @RequestParam("userIds") List<String> workspaceUserIdList,
		@RequestParam("product") String productName
	);
}
