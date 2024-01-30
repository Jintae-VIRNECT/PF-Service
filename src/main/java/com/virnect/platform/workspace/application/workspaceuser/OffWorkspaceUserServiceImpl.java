package com.virnect.platform.workspace.application.workspaceuser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;

import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.feign.rest.account.AccountRestServiceHandler;
import com.virnect.platform.feign.rest.account.dto.request.GuestMemberDeleteRequest;
import com.virnect.platform.feign.rest.account.dto.request.MemberDeleteRequest;
import com.virnect.platform.feign.rest.account.dto.request.UserInfoAccessCheckRequest;
import com.virnect.platform.feign.rest.account.dto.request.UserInfoModifyRequest;
import com.virnect.platform.feign.rest.account.dto.response.UserDeleteRestResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoAccessCheckResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse;
import com.virnect.platform.feign.rest.license.LicenseRestService;
import com.virnect.platform.feign.rest.license.dto.MyLicenseInfoListResponse;
import com.virnect.platform.feign.rest.license.dto.MyLicenseInfoResponse;
import com.virnect.platform.feign.rest.remote.RemoteRestService;
import com.virnect.platform.global.common.mapper.rest.RestMapStruct;
import com.virnect.platform.workspace.dao.workspace.WorkspaceRepository;
import com.virnect.platform.workspace.dao.workspacepermission.WorkspacePermissionRepository;
import com.virnect.platform.workspace.dao.workspacerole.WorkspaceRoleRepository;
import com.virnect.platform.workspace.dao.workspaceuser.WorkspaceUserRepository;
import com.virnect.platform.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.platform.workspace.domain.workspace.Role;
import com.virnect.platform.workspace.domain.workspace.UserType;
import com.virnect.platform.workspace.domain.workspace.Workspace;
import com.virnect.platform.workspace.domain.workspace.WorkspaceRole;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUser;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.platform.workspace.dto.request.MemberAccountDeleteRequest;
import com.virnect.platform.workspace.dto.request.MemberGuestDeleteRequest;
import com.virnect.platform.workspace.dto.request.MemberUpdateRequest;
import com.virnect.platform.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.platform.workspace.dto.response.MemberSeatDeleteResponse;
import com.virnect.platform.workspace.event.message.MailContextHandler;
import com.virnect.platform.workspace.exception.WorkspaceException;
import com.virnect.platform.workspace.global.common.ApiResponse;
import com.virnect.platform.workspace.global.error.ErrorCode;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Service
@Profile("onpremise")
public class OffWorkspaceUserServiceImpl extends WorkspaceUserService {
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceRoleRepository workspaceRoleRepository;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final AccountRestServiceHandler userRestServiceHandler;
	private final RemoteRestService remoteRestService;
	private final WorkspaceUserRepository workspaceUserRepository;

	public OffWorkspaceUserServiceImpl(
		WorkspaceRepository workspaceRepository,
		WorkspaceUserRepository workspaceUserRepository,
		WorkspaceRoleRepository workspaceRoleRepository,
		WorkspaceUserPermissionRepository workspaceUserPermissionRepository,
		MessageSource messageSource,
		LicenseRestService licenseRestService,
		RestMapStruct restMapStruct,
		ApplicationEventPublisher applicationEventPublisher,
		MailContextHandler mailContextHandler,
		WorkspacePermissionRepository workspacePermissionRepository,
		RemoteRestService remoteRestService,
		AccountRestServiceHandler userRestServiceHandler
	) {
		super(workspaceRepository, workspaceUserRepository, workspaceRoleRepository, workspaceUserPermissionRepository,
			messageSource, licenseRestService, restMapStruct, applicationEventPublisher, mailContextHandler,
			workspacePermissionRepository, remoteRestService, userRestServiceHandler
		);
		this.workspaceRepository = workspaceRepository;
		this.workspaceRoleRepository = workspaceRoleRepository;
		this.workspaceUserPermissionRepository = workspaceUserPermissionRepository;
		this.userRestServiceHandler = userRestServiceHandler;
		this.remoteRestService = remoteRestService;
		this.workspaceUserRepository = workspaceUserRepository;
	}

	@Override
	public ApiResponse<Boolean> reviseMemberInfo(
		String workspaceId, MemberUpdateRequest memberUpdateRequest, Locale locale
	) {
		// 변경 대상 유저 정보 조회
		UserInfoRestResponse updateUserInfo = userRestServiceHandler.getUserRequest(memberUpdateRequest.getUserId());
		if (updateUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}

		// 변경 요청 유저 permission 조회
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, memberUpdateRequest.getRequestUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		// 변경 대상 유저 permission 조회
		WorkspaceUserPermission updateUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, memberUpdateRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));

		//1. 사용자 닉네임 변경
		if (StringUtils.hasText(memberUpdateRequest.getNickname()) && !updateUserInfo.getNickname()
			.equals(memberUpdateRequest.getNickname())) {
			log.info("[REVISE MEMBER INFO] User nickname update. nickname : {}", memberUpdateRequest.getNickname());
			//1-1. 유저 타입 확인
			if (updateUserInfo.getUserType() == UserType.USER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
			}
			//1-2. 요청 유저 권한 확인
			if (!memberUpdateRequest.isUserSelfUpdateRequest()
				&& requestUserPermission.getWorkspaceRole().getRole() != Role.MASTER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
			//1-3. 변경 요청
			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.modifyUserRequest(
				memberUpdateRequest.getUserId(), new UserInfoModifyRequest(memberUpdateRequest.getNickname()));
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE);
			}
		}

		//2. 사용자 권한 변경
		if (StringUtils.hasText(memberUpdateRequest.getRole()) && !updateUserPermission.getWorkspaceRole()
			.getRole()
			.name()
			.equals(memberUpdateRequest.getRole())) {
			log.info(
				"[REVISE MEMBER INFO] User role update. current role : {}, update role : {} ",
				updateUserPermission.getWorkspaceRole().getRole(),
				memberUpdateRequest.getRole()
			);
			//2-1. 유저 타입 확인
			if (updateUserInfo.getUserType() == UserType.GUEST_USER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
			}
			//2-2. 요청 유저 권한 확인
			if (!memberUpdateRequest.isUserSelfUpdateRequest()
				&& requestUserPermission.getWorkspaceRole().getRole() != Role.MASTER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
			//2-3. 권한 변경
			WorkspaceRole updateWorkspaceRole = workspaceRoleRepository.findByRole(
					Role.valueOf(memberUpdateRequest.getRole()))
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_ROLE_NOT_FOUND));
			updateUserPermission.setWorkspaceRole(updateWorkspaceRole);
			workspaceUserPermissionRepository.save(updateUserPermission);

			//2-4. 메일 발송
			UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(
				updateUserPermission.getWorkspaceUser().getWorkspace().getUserId());
			if (masterUserInfo.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}

			//2-5. 히스토리 저장
			Workspace workspace = workspaceRepository.findByUuid(workspaceId)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
			saveUserRoleUpdateHistory(updateWorkspaceRole, masterUserInfo, updateUserInfo,
				memberUpdateRequest.getRequestUserId(), locale, workspace
			);
		}

		//3. 사용자 제품 라이선스 변경
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(
			workspaceId, memberUpdateRequest.getUserId());
		List<String> currentProductList = myLicenseInfoListResponse.getLicenseInfoList()
			.stream()
			.map(MyLicenseInfoResponse::getProductName)
			.collect(Collectors.toList());
		if (memberUpdateRequest.existLicenseUpdate(currentProductList)) {
			log.info(
				"[REVISE MEMBER INFO] User License update. current license : [{}]",
				org.apache.commons.lang.StringUtils.join(currentProductList, ",")
			);
			//3-1. 유저 타입 확인
			if (updateUserInfo.getUserType() == UserType.GUEST_USER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
			}
			//3-2. 요청 유저 권한 확인
			if (!memberUpdateRequest.isUserSelfUpdateRequest()) {
				if (updateUserPermission.getWorkspaceRole().getRole() == Role.MASTER) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_MASTER_PLAN);
				}
				if (!isMasterOrManagerRole(requestUserPermission.getWorkspaceRole())) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
				}
				if (isBothManagerRole(
					requestUserPermission.getWorkspaceRole(), updateUserPermission.getWorkspaceRole())) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
				}
			}

			//3-3. 제품 라이선스 부여/해제 요청
			List<String> addedProductList = new ArrayList<>(), removedProductList = new ArrayList<>();
			if (memberUpdateRequest.getLicenseRemote() != null && memberUpdateRequest.getLicenseRemote()
				&& !currentProductList.contains("REMOTE")) {
				grantWorkspaceLicenseToUser(workspaceId, updateUserInfo.getUuid(), "REMOTE");
				addedProductList.add("REMOTE");
			}
			if (memberUpdateRequest.getLicenseRemote() != null && !memberUpdateRequest.getLicenseRemote()
				&& currentProductList.contains("REMOTE")) {
				revokeWorkspaceLicenseToUser(workspaceId, updateUserInfo.getUuid(), "REMOTE");
				removedProductList.add("REMOTE");
			}
			if (memberUpdateRequest.getLicenseMake() != null && memberUpdateRequest.getLicenseMake()
				&& !currentProductList.contains("MAKE")) {
				grantWorkspaceLicenseToUser(workspaceId, updateUserInfo.getUuid(), "MAKE");
				addedProductList.add("MAKE");
			}

			if (memberUpdateRequest.getLicenseMake() != null && !memberUpdateRequest.getLicenseMake()
				&& currentProductList.contains("MAKE")) {
				revokeWorkspaceLicenseToUser(workspaceId, updateUserInfo.getUuid(), "MAKE");
				removedProductList.add("MAKE");
			}
			if (memberUpdateRequest.getLicenseView() != null && memberUpdateRequest.getLicenseView()
				&& !currentProductList.contains("VIEW")) {
				grantWorkspaceLicenseToUser(workspaceId, updateUserInfo.getUuid(), "VIEW");
				addedProductList.add("VIEW");
			}

			if (memberUpdateRequest.getLicenseView() != null && !memberUpdateRequest.getLicenseView()
				&& currentProductList.contains("VIEW")) {
				revokeWorkspaceLicenseToUser(workspaceId, updateUserInfo.getUuid(), "VIEW");
				removedProductList.add("VIEW");
			}

			//3-4. 라이선스 변경 히스토리 저장
			UserInfoRestResponse requestUserInfo = userRestServiceHandler.getUserRequest(
				memberUpdateRequest.getRequestUserId());
			if (requestUserInfo.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			Workspace workspace = workspaceRepository.findByUuid(workspaceId)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
			saveUserLicenseUpdateHistory(
				addedProductList, removedProductList, requestUserInfo, updateUserInfo, locale, workspace);

			log.info(
				"[REVISE MEMBER INFO] Revise License Result Info. Removed License Product Info >> [{}], Added License Product Info >> [{}].",
				org.apache.commons.lang.StringUtils.join(removedProductList, ","),
				org.apache.commons.lang.StringUtils.join(addedProductList, ",")
			);

			//3-5. 라이선스 변경 성공 메일 전송
		}
		return new ApiResponse<>(true);
	}

	@Override
	public ApiResponse<Boolean> inviteWorkspace(
		String workspaceId, WorkspaceInviteRequest workspaceInviteRequest, Locale locale
	) {
		return null;
	}

	@Override
	public RedirectView inviteWorkspaceAccept(String sessionCode, String lang) throws IOException {
		return null;
	}

	@Override
	public RedirectView inviteWorkspaceReject(String sessionCode, String lang) {
		return null;
	}

	@Override
	public boolean deleteWorkspaceMemberAccount(
		String workspaceId, MemberAccountDeleteRequest memberAccountDeleteRequest
	) {
		//구축형은 마스터의 비밀번호를 입력받음.
		if (StringUtils.isEmpty(memberAccountDeleteRequest.getRequestUserPassword())) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		//1-1. 삭제하려는 유저가 전용 계정인지 체크
		UserInfoRestResponse deleteUserInfo = userRestServiceHandler.getUserRequest(
			memberAccountDeleteRequest.getUserId());
		if (deleteUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		if (deleteUserInfo.getUserType() != UserType.WORKSPACE_ONLY_USER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_DELETE_FAIL);
		}
		//1-2. 마스터 비밀번호를 올바르게 입력했는지 체크
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(workspace.getUserId());
		if (masterUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		UserInfoAccessCheckRequest userInfoAccessCheckRequest = new UserInfoAccessCheckRequest(
			masterUserInfo.getEmail(), memberAccountDeleteRequest.getRequestUserPassword());
		ApiResponse<UserInfoAccessCheckResponse> apiResponse = userRestServiceHandler.accessCheckUserRequest(
			masterUserInfo.getUuid(), userInfoAccessCheckRequest);
		if (apiResponse.getCode() == 4001) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_DELETE_ACCESS);
		}
		if (apiResponse.getCode() != 200) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		//1-3. 요청한 사람의 권한 체크
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, memberAccountDeleteRequest.getRequestUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUserPermission deleteUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, memberAccountDeleteRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

		// 본인은 삭제할 수 없다.
		if (memberAccountDeleteRequest.isUserSelfUpdateRequest()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		// 요청한 사람이 마스터 또는 매니저여야 한다.
		if (!isMasterOrManagerRole(requestUserPermission.getWorkspaceRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
		// 매니저 유저는 매니저 유저를 삭제할 수 없다.
		if (isBothManagerRole(requestUserPermission.getWorkspaceRole(), deleteUserPermission.getWorkspaceRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//2. license-sever revoke api 요청
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(
			workspaceId, memberAccountDeleteRequest.getUserId());
		if (!myLicenseInfoListResponse.getLicenseInfoList().isEmpty()) {
			for (MyLicenseInfoResponse myLicenseInfoResponse : myLicenseInfoListResponse.getLicenseInfoList()) {
				revokeWorkspaceLicenseToUser(
					workspaceId, memberAccountDeleteRequest.getUserId(), myLicenseInfoResponse.getProductName());
			}
		}

		//3. user-server에 멤버 삭제 api 요청
		MemberDeleteRequest memberDeleteRequest = new MemberDeleteRequest();
		memberDeleteRequest.setMemberUserUUID(memberAccountDeleteRequest.getUserId());
		memberDeleteRequest.setMasterUUID(workspace.getUserId());
		UserDeleteRestResponse userDeleteRestResponse = userRestServiceHandler.deleteWorkspaceOnlyUserRequest(
			memberDeleteRequest);
		if (userDeleteRestResponse.isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		//4. workspace-sever 권한 및 소속 해제
		workspaceUserPermissionRepository.deleteAllByWorkspaceUser(deleteUserPermission.getWorkspaceUser());
		workspaceUserRepository.deleteById(deleteUserPermission.getWorkspaceUser().getId());

		log.info(
			"[DELETE WORKSPACE MEMBER ACCOUNT] Workspace delete user success. Request User UUID : [{}], Delete User UUID : [{}], DeleteDate : [{}]",
			memberAccountDeleteRequest.getRequestUserId(), memberAccountDeleteRequest.getUserId(), LocalDateTime.now()
		);
		return true;
	}

	@Override
	public MemberSeatDeleteResponse deleteWorkspaceMemberSeat(
		String workspaceId, MemberGuestDeleteRequest memberGuestDeleteRequest
	) {
		//구축형은 마스터의 비밀번호를 입력받음.
		if (StringUtils.isEmpty(memberGuestDeleteRequest.getRequestUserPassword())) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		//1-1. 요청한 사람의 권한 체크
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		checkGuestMemberManagementPermission(workspace, memberGuestDeleteRequest.getRequestUserId());

		//1-2. 요청받은 유저가 게스트 유저인지 체크
		WorkspaceUserPermission deleteUserPermission = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
				workspace, memberGuestDeleteRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		if (deleteUserPermission.getWorkspaceRole().getRole() != Role.GUEST) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_DELETE);
		}
		UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(
			memberGuestDeleteRequest.getUserId());
		if (userInfoRestResponse.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		if (userInfoRestResponse.getUserType() != UserType.GUEST_USER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_DELETE);
		}
		//1-3. 마스터 비밀번호를 올바르게 입력했는지 체크
		UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(workspace.getUserId());
		if (masterUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		UserInfoAccessCheckRequest userInfoAccessCheckRequest = new UserInfoAccessCheckRequest(
			masterUserInfo.getEmail(), memberGuestDeleteRequest.getRequestUserPassword());
		ApiResponse<UserInfoAccessCheckResponse> apiResponse = userRestServiceHandler.accessCheckUserRequest(
			masterUserInfo.getUuid(), userInfoAccessCheckRequest);
		if (apiResponse.getCode() == 4001) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_DELETE_ACCESS);
		}
		if (apiResponse.getCode() != 200) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		//2. remote 협업 종료를 위해 게스트 삭제 API 호출
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(
			workspaceId, memberGuestDeleteRequest.getUserId());
		if (myLicenseInfoListResponse.isHaveRemoteLicense()) {
			remoteRestService.sendGuestUserDeletedEvent(
				"DELETED_ACCOUNT", memberGuestDeleteRequest.getUserId(), workspaceId);
			log.info(
				"[REST - RM_SERVICE] Send guest user deleted event. deleted userId : {}, workspaceId : {}",
				memberGuestDeleteRequest.getUserId(), workspaceId
			);
		}

		//3. 라이선스 해제 요청
		if (!myLicenseInfoListResponse.isEmpty()) {
			myLicenseInfoListResponse.getLicenseInfoList()
				.forEach(myLicenseInfoResponse -> revokeWorkspaceLicenseToUser(workspaceId,
					memberGuestDeleteRequest.getUserId(), myLicenseInfoResponse.getProductName()
				));
		}

		//4. 게스트 유저 삭제 요청
		GuestMemberDeleteRequest guestMemberDeleteRequest = new GuestMemberDeleteRequest();
		guestMemberDeleteRequest.setMasterUUID(workspace.getUserId());
		guestMemberDeleteRequest.setGuestUserUUID(deleteUserPermission.getWorkspaceUser().getUserId());
		UserDeleteRestResponse userDeleteRestResponse = userRestServiceHandler.deleteGuestUserRequest(
			guestMemberDeleteRequest);
		if (userDeleteRestResponse.isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_DELETE);
		}

		//5. 웤스 서버에서 삭제
		WorkspaceUser workspaceUser = deleteUserPermission.getWorkspaceUser();
		workspaceUserPermissionRepository.delete(deleteUserPermission);
		workspaceUserRepository.delete(workspaceUser);

		return new MemberSeatDeleteResponse(true, memberGuestDeleteRequest.getUserId(), LocalDateTime.now());
	}

}
