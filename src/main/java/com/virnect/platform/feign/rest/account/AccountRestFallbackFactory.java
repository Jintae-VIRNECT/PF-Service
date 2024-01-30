package com.virnect.platform.feign.rest.account;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.feign.rest.account.dto.request.GuestMemberDeleteRequest;
import com.virnect.platform.feign.rest.account.dto.request.GuestMemberRegistrationRequest;
import com.virnect.platform.feign.rest.account.dto.request.MemberDeleteRequest;
import com.virnect.platform.feign.rest.account.dto.request.MemberRegistrationRequest;
import com.virnect.platform.feign.rest.account.dto.request.MemberUserPasswordChangeRequest;
import com.virnect.platform.feign.rest.account.dto.request.UserInfoAccessCheckRequest;
import com.virnect.platform.feign.rest.account.dto.request.UserInfoModifyRequest;
import com.virnect.platform.feign.rest.account.dto.response.InviteUserInfoResponse;
import com.virnect.platform.feign.rest.account.dto.response.MemberUserPasswordChangeResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserDeleteRestResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoAccessCheckResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoListRestResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserProfileUpdateResponse;
import com.virnect.platform.global.common.ApiResponse;
import com.virnect.platform.license.dto.rest.user.UserInfoRestResponse;

@Slf4j
@Service
public class AccountRestFallbackFactory implements FallbackFactory<AccountRestService> {
	@Override
	public AccountRestService create(Throwable cause) {
		return new AccountRestService() {
			@Override
			public ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> getUserInfoByUserId(String userId) {
				log.error("[USER_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
				log.error(cause.getMessage(), cause);
				ApiResponse apiResponse = new ApiResponse(null);
				apiResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return apiResponse;
			}

			@Override
			public ApiResponse<UserInfoRestResponse> getUserInfoByUserPrimaryId(long userId) {
				log.error("[USER_REST_SERVICE][FALL_BACK_FACTORY][ACTIVE]");
				log.error(cause.getMessage(), cause);
				ApiResponse apiResponse = new ApiResponse(null);
				apiResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return apiResponse;
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<InviteUserInfoResponse> getInviteUserInfoByEmail(String email) {
				return null;
			}

			public com.virnect.platform.workspace.global.common.ApiResponse<UserInfoListRestResponse> getUserInfoList(
				String search, List<String> workspaceUserIdList
			) {
				UserInfoListRestResponse userInfoListRestResponse = UserInfoListRestResponse.EMPTY;
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(userInfoListRestResponse);
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> registerMemberRequest(
				MemberRegistrationRequest memberRegistrationRequest, String serviceID
			) {
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(
					new com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse());
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<UserDeleteRestResponse> userDeleteRequest(
				MemberDeleteRequest memberDeleteRequest, String serviceID
			) {
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(new UserDeleteRestResponse());
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<UserInfoAccessCheckResponse> userInfoAccessCheckRequest(
				String userId, UserInfoAccessCheckRequest userInfoAccessCheckRequest
			) {
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(new UserInfoAccessCheckResponse());
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<MemberUserPasswordChangeResponse> memberUserPasswordChangeRequest(
				MemberUserPasswordChangeRequest memberUserPasswordChangeRequest, String serviceID
			) {
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(
					new MemberUserPasswordChangeResponse(false, "", "", LocalDateTime.now())
				);
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> modifyUserInfoRequest(
				String userId, UserInfoModifyRequest userInfoModifyRequest
			) {
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(
					new com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse());
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<UserProfileUpdateResponse> modifyUserProfileRequest(
				String userId, MultipartFile profile, Boolean updateAsDefaultImage
			) {
				return new com.virnect.platform.workspace.global.common.ApiResponse<>(new UserProfileUpdateResponse());
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> guestMemberRegistrationRequest(
				GuestMemberRegistrationRequest guestMemberRegistrationRequest, String serviceID
			) {
				return null;
			}

			@Override
			public com.virnect.platform.workspace.global.common.ApiResponse<UserDeleteRestResponse> guestMemberDeleteRequest(
				GuestMemberDeleteRequest guestMemberDeleteRequest, String serviceID
			) {
				return null;
			}
		};
	}
}
