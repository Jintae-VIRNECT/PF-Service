package com.virnect.workspace.application.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.application.user.dto.request.GuestMemberDeleteRequest;
import com.virnect.workspace.application.user.dto.request.GuestMemberRegistrationRequest;
import com.virnect.workspace.application.user.dto.request.MemberDeleteRequest;
import com.virnect.workspace.application.user.dto.request.MemberRegistrationRequest;
import com.virnect.workspace.application.user.dto.request.MemberUserPasswordChangeRequest;
import com.virnect.workspace.application.user.dto.request.UserInfoAccessCheckRequest;
import com.virnect.workspace.application.user.dto.request.UserInfoModifyRequest;
import com.virnect.workspace.application.user.dto.response.InviteUserInfoResponse;
import com.virnect.workspace.application.user.dto.response.MemberUserPasswordChangeResponse;
import com.virnect.workspace.application.user.dto.response.UserDeleteRestResponse;
import com.virnect.workspace.application.user.dto.response.UserInfoAccessCheckResponse;
import com.virnect.workspace.application.user.dto.response.UserInfoListRestResponse;
import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.application.user.dto.response.UserProfileUpdateResponse;
import com.virnect.workspace.global.common.ApiResponse;

/**
 * Project: PF-Workspace
 * DATE: 2020-04-29
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Component
public class UserRestFallbackFactory implements FallbackFactory<UserRestService> {

	@Override
	public UserRestService create(Throwable cause) {
		log.error(cause.getMessage(), cause);
		return new UserRestService() {
			@Override
			public ApiResponse<UserInfoRestResponse> getUserInfoByUserId(String userId) {
				return new ApiResponse<>(new UserInfoRestResponse());
			}

			@Override
			public ApiResponse<InviteUserInfoResponse> getInviteUserInfoByEmail(String email) {
				return new ApiResponse<>(null);
			}

			@Override
			public ApiResponse<UserInfoListRestResponse> getUserInfoList(String search, List<String> workspaceUserIdList) {
				UserInfoListRestResponse userInfoListRestResponse = UserInfoListRestResponse.EMPTY;
				return new ApiResponse<>(userInfoListRestResponse);
			}

			@Override
			public ApiResponse<UserInfoRestResponse> registerMemberRequest(
				MemberRegistrationRequest memberRegistrationRequest, String serviceID
			) {
				return new ApiResponse<>(new UserInfoRestResponse());
			}

			@Override
			public ApiResponse<UserDeleteRestResponse> userDeleteRequest(MemberDeleteRequest memberDeleteRequest, String serviceID) {
				return new ApiResponse<>(new UserDeleteRestResponse());
			}

			@Override
			public ApiResponse<UserInfoAccessCheckResponse> userInfoAccessCheckRequest(
				String userId, UserInfoAccessCheckRequest userInfoAccessCheckRequest
			) {
				return new ApiResponse<>(new UserInfoAccessCheckResponse());
			}

			@Override
			public ApiResponse<MemberUserPasswordChangeResponse> memberUserPasswordChangeRequest(
				MemberUserPasswordChangeRequest memberUserPasswordChangeRequest, String serviceID
			) {
				return new ApiResponse<>(
					new MemberUserPasswordChangeResponse(false, "", "", LocalDateTime.now())
				);
			}

			@Override
			public ApiResponse<UserInfoRestResponse> modifyUserInfoRequest(String userId, UserInfoModifyRequest userInfoModifyRequest) {
				return new ApiResponse<>(new UserInfoRestResponse());
			}

			@Override
			public ApiResponse<UserProfileUpdateResponse> modifyUserProfileRequest(
				String userId, MultipartFile profile, Boolean updateAsDefaultImage
			) {
				return new ApiResponse<>(new UserProfileUpdateResponse());
			}

			@Override
			public ApiResponse<UserInfoRestResponse> guestMemberRegistrationRequest(
				GuestMemberRegistrationRequest guestMemberRegistrationRequest, String serviceID
			) {
				return null;
			}

			@Override
			public ApiResponse<UserDeleteRestResponse> guestMemberDeleteRequest(
				GuestMemberDeleteRequest guestMemberDeleteRequest, String serviceID
			) {
				return null;
			}
		};
	}
}
