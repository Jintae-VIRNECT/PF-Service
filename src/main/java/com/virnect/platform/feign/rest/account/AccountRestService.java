package com.virnect.platform.feign.rest.account;

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

/**
 * Project: PF-Workspace
 * DATE: 2020-01-17
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */

@FeignClient(name = "account-server", fallbackFactory = AccountRestFallbackFactory.class)
public interface AccountRestService {
	/**
	 * 유저 정보 조회
	 *
	 * @param userId - 유저 고유 아이디
	 * @return - 유저 정보
	 */
	@GetMapping("/users/{userId}")
	ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> getUserInfoByUserId(@PathVariable("userId") String userId);

	/**
	 * 유저 정보 조회
	 *
	 * @param userId - 유저 고유 아이디
	 * @return - 유저 정보
	 */
	@GetMapping("/users/billing/{userId}")
	ApiResponse<UserInfoRestResponse> getUserInfoByUserPrimaryId(@PathVariable("userId") long userId);

	//유저 중복 여부 조회
	@GetMapping("/users/invite")
	com.virnect.platform.workspace.global.common.ApiResponse<InviteUserInfoResponse> getInviteUserInfoByEmail(@RequestParam("email") String email);

	//사용자 정보 목록 조회
	@PostMapping("/users/list")
	com.virnect.platform.workspace.global.common.ApiResponse<UserInfoListRestResponse> getUserInfoList(
		@RequestParam("search") String search, @RequestBody List<String> workspaceUserIdList
	);

	//멤버 등록
	@PostMapping("/users/members")
	com.virnect.platform.workspace.global.common.ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> registerMemberRequest(
		@RequestBody MemberRegistrationRequest memberRegistrationRequest, @RequestHeader("serviceID") String serviceID
	);

	//멤버 삭제
	@DeleteMapping("/users/members")
	com.virnect.platform.workspace.global.common.ApiResponse<UserDeleteRestResponse> userDeleteRequest(
		@RequestBody MemberDeleteRequest memberDeleteRequest, @RequestHeader("serviceID") String serviceID
	);

	//개인정보 접근 인증
	@PostMapping("/users/{userId}/access")
	com.virnect.platform.workspace.global.common.ApiResponse<UserInfoAccessCheckResponse> userInfoAccessCheckRequest(
		@PathVariable("userId") String userId, @RequestBody UserInfoAccessCheckRequest userInfoAccessCheckRequest
	);

	//멤버 비밀번호 변경
	@PostMapping("/users/members/password")
	com.virnect.platform.workspace.global.common.ApiResponse<MemberUserPasswordChangeResponse> memberUserPasswordChangeRequest(
		@RequestBody MemberUserPasswordChangeRequest memberUserPasswordChangeRequest, @RequestHeader("serviceID") String serviceID
	);

	//개인 정보 수정
	@PostMapping("/users/{userId}")
	com.virnect.platform.workspace.global.common.ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> modifyUserInfoRequest(
		@PathVariable("userId") String userId, @RequestBody UserInfoModifyRequest userInfoModifyRequest
	);

	//프로필 변경
	@RequestMapping(method = RequestMethod.POST, value = "/users/{userId}/profile", consumes = "multipart/form-data")
	com.virnect.platform.workspace.global.common.ApiResponse<UserProfileUpdateResponse> modifyUserProfileRequest(
		@PathVariable("userId") String userId, @RequestPart("profile") MultipartFile profile,
		@RequestParam("updateAsDefaultImage") Boolean updateAsDefaultImage
	);

	//워크스페이스 게스트 계정 등록
	@PostMapping("/users/members/guest")
	com.virnect.platform.workspace.global.common.ApiResponse<com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse> guestMemberRegistrationRequest(
		@RequestBody GuestMemberRegistrationRequest guestMemberRegistrationRequest, @RequestHeader("serviceID") String serviceID
	);

	//워크스페이스 게스트 계정 삭제
	@DeleteMapping("/users/members/guest")
	com.virnect.platform.workspace.global.common.ApiResponse<UserDeleteRestResponse> guestMemberDeleteRequest(
		@RequestBody GuestMemberDeleteRequest guestMemberDeleteRequest, @RequestHeader("serviceID") String serviceID
	);
}

