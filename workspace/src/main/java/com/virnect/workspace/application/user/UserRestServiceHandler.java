package com.virnect.workspace.application.user;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
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
 * DATE: 2021-12-06
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class UserRestServiceHandler {
	private static final String PREFIX_USER_REST_SERVICE = "REST - USER_SERVER";
	private static final String SERVICE_ID = "workspace-server";
	private final UserRestService userRestService;

	public UserInfoRestResponse getUserRequest(String userId) {
		ApiResponse<UserInfoRestResponse> response = userRestService.getUserInfoByUserId(userId);
		if (response.getCode() != 200 || response.getData() == null || response.getData()
			.isEmtpy()) {
			log.error(
				"[{}][GET USER INFO] userId : {}, response code : {}, response message : {}", PREFIX_USER_REST_SERVICE,
				userId, response.getCode(), response.getMessage()
			);
			return UserInfoRestResponse.EMPTY;
		}
		return response.getData();
	}

	public UserInfoListRestResponse getUserListRequest(String search, List<String> userIdList) {
		ApiResponse<UserInfoListRestResponse> response = userRestService.getUserInfoList(
			search, userIdList);
		if (response.getCode() != 200 || response.getData() == null || response.getData().isEmpty()) {
			log.error(
				"[{}][GET USER INFO LIST] search : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, search,
				response.getCode(), response.getMessage()
			);
			return UserInfoListRestResponse.EMPTY;
		}
		return response.getData();
	}

	public UserInfoRestResponse modifyUserRequest(String userId, UserInfoModifyRequest userInfoModifyRequest) {
		ApiResponse<UserInfoRestResponse> response = userRestService.modifyUserInfoRequest(
			userId, userInfoModifyRequest);
		if (response.getCode() != 200 || response.getData() == null || response.getData().isEmtpy()) {
			log.error("[{}][MODIFY USER INFO] userId : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, userId, response.getCode(), response.getMessage()
			);
			return UserInfoRestResponse.EMPTY;
			//throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE);
		}
		log.info("[{}][MODIFY USER INFO] userId : {}, response code : {}, response data : {}", PREFIX_USER_REST_SERVICE,
			userId, response.getCode(), response.getData().toString()
		);
		return response.getData();
	}

	public UserInfoRestResponse registerUserRequest(MemberRegistrationRequest memberRegistrationRequest) {
		ApiResponse<UserInfoRestResponse> response = userRestService.registerMemberRequest(
			memberRegistrationRequest, SERVICE_ID);
		if (response.getCode() != 200 || response.getData() == null || response.getData().isEmtpy()) {
			log.error(
				"[{}][REGISTRATION USER] email : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, memberRegistrationRequest.getEmail(), response.getCode(),
				response.getMessage()
			);
			return UserInfoRestResponse.EMPTY;
		}
		return response.getData();
	}

	public ApiResponse<UserInfoAccessCheckResponse> accessCheckUserRequest(
		String userId, UserInfoAccessCheckRequest userInfoAccessCheckRequest
	) {
		ApiResponse<UserInfoAccessCheckResponse> response = userRestService.userInfoAccessCheckRequest(
			userId, userInfoAccessCheckRequest);
		if (response.getCode() != 200 || response.getData() == null || !response.getData().isAccessCheckResult()) {
			log.error(
				"[{}][ACCESS CHECK USER ACCOUNT] userId : {}, request : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, userId, userInfoAccessCheckRequest.toString(), response.getCode(),
				response.getMessage()
			);
		}
		return response;
	}

	public UserInfoRestResponse guestUserRegistrationRequest(
		GuestMemberRegistrationRequest guestMemberRegistrationRequest
	) {
		ApiResponse<UserInfoRestResponse> response = userRestService.guestMemberRegistrationRequest(
			guestMemberRegistrationRequest, SERVICE_ID);
		if (response.getCode() != 200 || response.getData() == null || response.getData().isEmtpy()) {
			log.error(
				"[{}][REGISTRATION GUEST_USER] request : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, guestMemberRegistrationRequest.toString(),
				response.getCode(), response.getMessage()
			);
			return UserInfoRestResponse.EMPTY;
		}
		return response.getData();
	}

	public MemberUserPasswordChangeResponse userPasswordChangeRequest(
		MemberUserPasswordChangeRequest changeRequest
	) {
		ApiResponse<MemberUserPasswordChangeResponse> response = userRestService.memberUserPasswordChangeRequest(
			changeRequest, SERVICE_ID);
		if (response.getCode() != 200 || response.getData() == null || !response.getData().isChanged()) {
			log.error(
				"[{}][USER PASSWORD CHANGE] request : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE,
				changeRequest.toString(),
				response.getCode(), response.getMessage()
			);
			return MemberUserPasswordChangeResponse.EMPTY;
		}
		log.info(
			"[{}][USER PASSWORD CHANGE] request : {}, response code : {}, response data : {}",
			PREFIX_USER_REST_SERVICE, changeRequest.toString(), response.getCode(), response.getData().toString()
		);
		return response.getData();
	}

	public UserDeleteRestResponse deleteGuestUserRequest(GuestMemberDeleteRequest guestMemberDeleteRequest) {
		ApiResponse<UserDeleteRestResponse> response = userRestService.guestMemberDeleteRequest(
			guestMemberDeleteRequest, SERVICE_ID);
		if (response.getCode() != 200 || response.getData() == null) {
			log.error(
				"[{}][DELETE GUEST_USER] request : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, guestMemberDeleteRequest.toString(),
				response.getCode(), response.getMessage()
			);
			return UserDeleteRestResponse.EMPTY;
		}
		log.info(
			"[{}][DELETE GUEST_USER] request : {}, response code : {}, response data : {}",
			PREFIX_USER_REST_SERVICE, guestMemberDeleteRequest.toString(),
			response.getCode(), response.getData().toString()
		);
		return response.getData();
	}

	public UserDeleteRestResponse deleteWorkspaceOnlyUserRequest(MemberDeleteRequest memberDeleteRequest) {
		ApiResponse<UserDeleteRestResponse> apiResponse = userRestService.userDeleteRequest(
			memberDeleteRequest, SERVICE_ID);
		if (apiResponse.getCode() != 200 || apiResponse.getData() == null) {
			log.error(
				"[{}][DELETE WORKSPACE_ONLY_USER] request : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE,
				memberDeleteRequest.toString(), apiResponse.getCode(), apiResponse.getMessage()
			);
			return UserDeleteRestResponse.EMPTY;
		}
		log.info(
			"[{}][DELETE WORKSPACE_ONLY_USER] request : {}, response code : {}, response data : {}",
			PREFIX_USER_REST_SERVICE,
			memberDeleteRequest.toString(), apiResponse.getCode(), apiResponse.getData()
		);
		return apiResponse.getData();
	}

	public UserProfileUpdateResponse modifyUserDefaultProfileRequest(
		String userId
	) {
		ApiResponse<UserProfileUpdateResponse> response = userRestService.modifyUserProfileRequest(
			userId, null, true);
		if (response.getCode() != 200) {
			log.error(
				"[{}][UPDATE USER DEFAULT PROFILE] userId : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, userId, response.getCode(), response.getMessage()
			);
			return UserProfileUpdateResponse.EMPTY;
		}
		return response.getData();
	}

	public UserProfileUpdateResponse modifyUserProfileRequest(
		String userId, MultipartFile profile
	) {
		ApiResponse<UserProfileUpdateResponse> apiResponse = userRestService.modifyUserProfileRequest(
			userId, profile, false);
		if (apiResponse.getCode() != 200) {
			log.error(
				"[{}][UPDATE USER PROFILE] userId : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, userId, apiResponse.getCode(), apiResponse.getMessage()
			);
			return UserProfileUpdateResponse.EMPTY;
		}
		return apiResponse.getData();
	}

	public ApiResponse<InviteUserInfoResponse> getInviteUserRequest(String email) {
		ApiResponse<InviteUserInfoResponse> response = userRestService.getInviteUserInfoByEmail(
			email);
		if (response.getCode() != 200 || response.getData() == null) {
			log.error(
				"[{}][GET INVITED USER] email : {}, response code : {}, response message : {}",
				PREFIX_USER_REST_SERVICE, email,
				response.getCode(), response.getMessage()
			);
		}
		return response;
	}
}
