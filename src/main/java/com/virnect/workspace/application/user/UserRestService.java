package com.virnect.workspace.application.user;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

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
import com.virnect.workspace.global.config.FeignConfiguration;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-17
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@FeignClient(name = "account-server", fallbackFactory = UserRestFallbackFactory.class, configuration = FeignConfiguration.class)
public interface UserRestService {
	//유저 정보 조회
	@GetMapping("/users/{userId}")
	ApiResponse<UserInfoRestResponse> getUserInfoByUserId(@PathVariable("userId") String userId);

	//유저 중복 여부 조회
	@GetMapping("/users/invite")
	ApiResponse<InviteUserInfoResponse> getInviteUserInfoByEmail(@RequestParam("email") String email);

	//사용자 정보 목록 조회
	@PostMapping("/users/list")
	ApiResponse<UserInfoListRestResponse> getUserInfoList(
		@RequestParam("search") String search, @RequestBody List<String> workspaceUserIdList
	);

	//멤버 등록
	@PostMapping("/users/members")
	ApiResponse<UserInfoRestResponse> registerMemberRequest(
		@RequestBody MemberRegistrationRequest memberRegistrationRequest, @RequestHeader("serviceID") String serviceID
	);

	//멤버 삭제
	@DeleteMapping("/users/members")
	ApiResponse<UserDeleteRestResponse> userDeleteRequest(
		@RequestBody MemberDeleteRequest memberDeleteRequest, @RequestHeader("serviceID") String serviceID
	);

	//개인정보 접근 인증
	@PostMapping("/users/{userId}/access")
	ApiResponse<UserInfoAccessCheckResponse> userInfoAccessCheckRequest(
		@PathVariable("userId") String userId, @RequestBody UserInfoAccessCheckRequest userInfoAccessCheckRequest
	);

	//멤버 비밀번호 변경
	@PostMapping("/users/members/password")
	ApiResponse<MemberUserPasswordChangeResponse> memberUserPasswordChangeRequest(
		@RequestBody MemberUserPasswordChangeRequest memberUserPasswordChangeRequest, @RequestHeader("serviceID") String serviceID
	);

	//개인 정보 수정
	@PostMapping("/users/{userId}")
	ApiResponse<UserInfoRestResponse> modifyUserInfoRequest(
		@PathVariable("userId") String userId, @RequestBody UserInfoModifyRequest userInfoModifyRequest
	);

	//프로필 변경
	@RequestMapping(method = RequestMethod.POST, value = "/users/{userId}/profile", consumes = "multipart/form-data")
	ApiResponse<UserProfileUpdateResponse> modifyUserProfileRequest(
		@PathVariable("userId") String userId, @RequestPart("profile") MultipartFile profile,
		@RequestParam("updateAsDefaultImage") Boolean updateAsDefaultImage
	);

	//워크스페이스 게스트 계정 등록
	@PostMapping("/users/members/guest")
	ApiResponse<UserInfoRestResponse> guestMemberRegistrationRequest(
		@RequestBody GuestMemberRegistrationRequest guestMemberRegistrationRequest, @RequestHeader("serviceID") String serviceID
	);

	//워크스페이스 게스트 계정 삭제
	@DeleteMapping("/users/members/guest")
	ApiResponse<UserDeleteRestResponse> guestMemberDeleteRequest(
		@RequestBody GuestMemberDeleteRequest guestMemberDeleteRequest, @RequestHeader("serviceID") String serviceID
	);
}

