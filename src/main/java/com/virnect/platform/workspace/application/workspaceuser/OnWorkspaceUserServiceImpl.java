package com.virnect.platform.workspace.application.workspaceuser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.Context;

import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.feign.rest.account.AccountRestServiceHandler;
import com.virnect.platform.feign.rest.account.dto.request.GuestMemberDeleteRequest;
import com.virnect.platform.feign.rest.account.dto.request.MemberDeleteRequest;
import com.virnect.platform.feign.rest.account.dto.request.UserInfoModifyRequest;
import com.virnect.platform.feign.rest.account.dto.response.InviteUserDetailInfoResponse;
import com.virnect.platform.feign.rest.account.dto.response.InviteUserInfoResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserDeleteRestResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse;
import com.virnect.platform.feign.rest.license.LicenseRestService;
import com.virnect.platform.feign.rest.license.dto.MyLicenseInfoListResponse;
import com.virnect.platform.feign.rest.license.dto.MyLicenseInfoResponse;
import com.virnect.platform.feign.rest.license.dto.WorkspaceLicensePlanInfoResponse;
import com.virnect.platform.feign.rest.remote.RemoteRestService;
import com.virnect.platform.global.common.mapper.rest.RestMapStruct;
import com.virnect.platform.workspace.dao.cache.UserInviteRepository;
import com.virnect.platform.workspace.dao.workspace.WorkspaceRepository;
import com.virnect.platform.workspace.dao.workspacepermission.WorkspacePermissionRepository;
import com.virnect.platform.workspace.dao.workspacerole.WorkspaceRoleRepository;
import com.virnect.platform.workspace.dao.workspaceuser.WorkspaceUserRepository;
import com.virnect.platform.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.platform.workspace.domain.redis.UserInvite;
import com.virnect.platform.workspace.domain.workspace.Role;
import com.virnect.platform.workspace.domain.workspace.UserType;
import com.virnect.platform.workspace.domain.workspace.Workspace;
import com.virnect.platform.workspace.domain.workspace.WorkspacePermission;
import com.virnect.platform.workspace.domain.workspace.WorkspaceRole;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUser;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.platform.workspace.dto.request.MemberAccountDeleteRequest;
import com.virnect.platform.workspace.dto.request.MemberGuestDeleteRequest;
import com.virnect.platform.workspace.dto.request.MemberUpdateRequest;
import com.virnect.platform.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.platform.workspace.dto.response.MemberSeatDeleteResponse;
import com.virnect.platform.workspace.event.history.HistoryAddEvent;
import com.virnect.platform.workspace.event.invite.InviteSessionDeleteEvent;
import com.virnect.platform.workspace.event.message.MailContextHandler;
import com.virnect.platform.workspace.event.message.MailSendEvent;
import com.virnect.platform.workspace.exception.WorkspaceException;
import com.virnect.platform.workspace.global.common.ApiResponse;
import com.virnect.platform.workspace.global.common.RedirectProperty;
import com.virnect.platform.workspace.global.constant.Mail;
import com.virnect.platform.workspace.global.constant.Permission;
import com.virnect.platform.workspace.global.constant.RedirectPath;
import com.virnect.platform.workspace.global.constant.UUIDType;
import com.virnect.platform.workspace.global.error.ErrorCode;
import com.virnect.platform.workspace.global.util.RandomStringTokenUtil;

/**
 * Project: PF-Workspace
 * DATE: 2021-02-02
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Service
@Profile("!onpremise")
public class OnWorkspaceUserServiceImpl extends WorkspaceUserService {
	private static final int MAX_JOIN_WORKSPACE_AMOUNT = 49;//최대 참여 가능한 워크스페이스 수
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceUserRepository workspaceUserRepository;
	private final WorkspaceRoleRepository workspaceRoleRepository;
	private final WorkspacePermissionRepository workspacePermissionRepository;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final UserInviteRepository userInviteRepository;
	private final MessageSource messageSource;
	private final RedirectProperty redirectProperty;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final MailContextHandler mailContextHandler;
	private final AccountRestServiceHandler userRestServiceHandler;
	private final RemoteRestService remoteRestService;

	public OnWorkspaceUserServiceImpl(
		WorkspaceRepository workspaceRepository, WorkspaceUserRepository workspaceUserRepository,
		WorkspaceRoleRepository workspaceRoleRepository,
		WorkspaceUserPermissionRepository workspaceUserPermissionRepository,
		MessageSource messageSource, LicenseRestService licenseRestService, RestMapStruct restMapStruct,
		ApplicationEventPublisher applicationEventPublisher,
		MailContextHandler mailContextHandler,
		WorkspacePermissionRepository workspacePermissionRepository, UserInviteRepository userInviteRepository,
		RedirectProperty redirectProperty,
		RemoteRestService remoteRestService, AccountRestServiceHandler userRestServiceHandler
	) {
		super(
			workspaceRepository, workspaceUserRepository, workspaceRoleRepository, workspaceUserPermissionRepository,
			messageSource, licenseRestService, restMapStruct, applicationEventPublisher,
			mailContextHandler, workspacePermissionRepository, remoteRestService, userRestServiceHandler
		);
		this.workspaceRepository = workspaceRepository;
		this.workspaceUserRepository = workspaceUserRepository;
		this.workspaceRoleRepository = workspaceRoleRepository;
		this.workspacePermissionRepository = workspacePermissionRepository;
		this.workspaceUserPermissionRepository = workspaceUserPermissionRepository;
		this.userInviteRepository = userInviteRepository;
		this.messageSource = messageSource;
		this.redirectProperty = redirectProperty;
		this.applicationEventPublisher = applicationEventPublisher;
		this.mailContextHandler = mailContextHandler;
		this.userRestServiceHandler = userRestServiceHandler;
		this.remoteRestService = remoteRestService;
	}

	@Override
	public ApiResponse<Boolean> reviseMemberInfo(
		String workspaceId, MemberUpdateRequest memberUpdateRequest, Locale locale
	) {
		//1. 사용자 닉네임 변경
		if (StringUtils.hasText(memberUpdateRequest.getNickname())) {
			log.info("[REVISE MEMBER INFO] User nickname update. nickname : {}", memberUpdateRequest.getNickname());
			//1-1. 유저 타입 확인
			UserInfoRestResponse updateUserInfo = userRestServiceHandler.getUserRequest(
				memberUpdateRequest.getUserId());
			if (updateUserInfo.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			if (updateUserInfo.getUserType() == UserType.USER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
			}
			//1-2. 요청 유저 권한 확인
			WorkspaceRole requestUserRole = workspaceRoleRepository.findWorkspaceRole(
					workspaceId, memberUpdateRequest.getRequestUserId())
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
			if (!memberUpdateRequest.isUserSelfUpdateRequest() && requestUserRole.getRole() != Role.MASTER) {
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
		if (StringUtils.hasText(memberUpdateRequest.getRole())) {
			WorkspaceUserPermission updateUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
					workspaceId, memberUpdateRequest.getUserId())
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
			log.info(
				"[REVISE MEMBER INFO] User role update. current role : {}, update role : {} ",
				updateUserPermission.getWorkspaceRole().getRole(),
				memberUpdateRequest.getRole()
			);
			//2-1. 유저 타입 확인
			UserInfoRestResponse updateUserInfo = userRestServiceHandler.getUserRequest(
				memberUpdateRequest.getUserId());
			if (updateUserInfo.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			if (updateUserInfo.getUserType() == UserType.GUEST_USER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
			}
			//2-2. 요청 유저 권한 확인
			WorkspaceRole requestUserRole = workspaceRoleRepository.findWorkspaceRole(
					workspaceId, memberUpdateRequest.getRequestUserId())
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
			if (!memberUpdateRequest.isUserSelfUpdateRequest() && requestUserRole.getRole() != Role.MASTER) {
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
			Context context = mailContextHandler.getWorkspaceUserPermissionUpdateContext(
				updateUserPermission.getWorkspaceUser().getWorkspace().getName(), masterUserInfo, updateUserInfo,
				updateWorkspaceRole.getRole().toString()
			);
			applicationEventPublisher.publishEvent(
				new MailSendEvent(context, Mail.WORKSPACE_USER_PERMISSION_UPDATE, locale, updateUserInfo.getEmail()));

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
			UserInfoRestResponse updateUserInfo = userRestServiceHandler.getUserRequest(
				memberUpdateRequest.getUserId());
			if (updateUserInfo.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			if (updateUserInfo.getUserType() == UserType.GUEST_USER) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
			}
			//3-2. 요청 유저 권한 확인
			if (!memberUpdateRequest.isUserSelfUpdateRequest()) {
				WorkspaceRole requestUserRole = workspaceRoleRepository.findWorkspaceRole(
						workspaceId, memberUpdateRequest.getRequestUserId())
					.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
				WorkspaceRole updateUserRole = workspaceRoleRepository.findWorkspaceRole(
						workspaceId, memberUpdateRequest.getUserId())
					.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
				if (updateUserRole.getRole() == Role.MASTER) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_MASTER_PLAN);
				}
				if (!isMasterOrManagerRole(requestUserRole)) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
				}
				if (isBothManagerRole(requestUserRole, updateUserRole)) {
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
			UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(workspace.getUserId());
			if (masterUserInfo.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			Context context = mailContextHandler.getWorkspaceUserPlanUpdateContext(
				workspace.getName(), masterUserInfo, updateUserInfo, currentProductList);
			applicationEventPublisher.publishEvent(
				new MailSendEvent(context, Mail.WORKSPACE_USER_PLAN_UPDATE, locale, updateUserInfo.getEmail()));
		}
		return new ApiResponse<>(true);
	}

	@Override
	public ApiResponse<Boolean> inviteWorkspace(
		String workspaceId, WorkspaceInviteRequest workspaceInviteRequest, Locale locale
	) {
		//1-1. 초대 받은 유저 역할 체크 : 마스터 권한을 부여하여 초대할 수 없다.
		if (workspaceInviteRequest.existMasterUserInvite()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE);
		}

		//1-2. 초대 요청 유저 역할 체크
		checkWorkspaceInvitePermission(
			workspaceId, workspaceInviteRequest.getUserId(), workspaceInviteRequest.getUserInfoList());

		//1-3. 워크스페이스에 최대 참여 가능한 유저 수 체크
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = getWorkspaceLicensesByWorkspaceId(
			workspaceId);
		checkWorkspaceMaxUserAmount(
			workspaceId, workspaceInviteRequest.getUserInfoList().size(), workspaceLicensePlanInfoResponse);

		//1-4. 제품 라이선스를 부여할 수 있는 지 체크
		checkWorkspaceInviteLicenseProduct(
			workspaceInviteRequest.getUserInfoList(), workspaceLicensePlanInfoResponse);

		//1-5. 탈퇴한 유저인지 체크
		List<String> invitedUserEmailList = workspaceInviteRequest.getUserInfoList()
			.stream()
			.map(WorkspaceInviteRequest.UserInfo::getEmail)
			.collect(Collectors.toList());
		Map<String, InviteUserInfoResponse> inviteUserInfoResponseMap = checkWorkspaceInvitedUserSecession(
			invitedUserEmailList);

		//1-6. 이미 초대된 유저인지 체크
		checkWorkspaceInvitedUserAlreadyJoin(workspaceId, inviteUserInfoResponseMap);

		//1-7. 이미 최대 참여 가능한 워크스페이스를 넘긴 유저인지 체크
		checkWorkspaceInvitedUserMaxJoin(inviteUserInfoResponseMap);

		// 마스터 유저 정보
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		UserInfoRestResponse masterUser = userRestServiceHandler.getUserRequest(workspace.getUserId());
		if (masterUser.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}

		//2. 초대 정보 저장
		workspaceInviteRequest.getUserInfoList().forEach(userInfo -> {
			InviteUserInfoResponse inviteUserResponse = inviteUserInfoResponseMap.get(userInfo.getEmail());
			String sessionCode = RandomStringTokenUtil.generate(UUIDType.INVITE_CODE, 20);
			Optional<UserInvite> userInviteOptional = userInviteRepository.findByWorkspaceIdAndInvitedUserEmail(
				workspaceId, userInfo.getEmail());
			if (userInviteOptional.isPresent()) {
				UserInvite userInvite = userInviteOptional.get();
				userInvite.setRole(userInfo.getRole());
				userInvite.setPlanRemote(userInfo.isPlanRemote());
				userInvite.setPlanMake(userInfo.isPlanMake());
				userInvite.setPlanView(userInfo.isPlanView());
				userInvite.setUpdatedDate(LocalDateTime.now());
				userInvite.setExpireTime(Duration.ofDays(7).getSeconds());
				userInviteRepository.save(userInvite);
				sessionCode = userInvite.getSessionCode();
				log.info("[WORKSPACE INVITE USER] Workspace Invite Info Redis Update >> {}", userInvite);
			} else {
				UserInvite newUserInvite = UserInvite.builder()
					.sessionCode(sessionCode)
					.invitedUserEmail(userInfo.getEmail())
					.invitedUserId(inviteUserResponse.getInviteUserDetailInfo().getUserUUID())
					.requestUserId(workspaceInviteRequest.getUserId())
					.workspaceId(workspaceId)
					.role(userInfo.getRole())
					.planRemote(userInfo.isPlanRemote())
					.planMake(userInfo.isPlanMake())
					.planView(userInfo.isPlanView())
					.planRemoteType(null)
					.planMakeType(null)
					.planViewType(null)
					.invitedDate(LocalDateTime.now())
					.updatedDate(null)
					.expireTime(Duration.ofDays(7).getSeconds())
					.build();
				userInviteRepository.save(newUserInvite);
				log.info("[WORKSPACE INVITE USER] Workspace Invite Info Redis Set >> {}", newUserInvite);
			}

			//3. 초대 메일 전송
			List<String> emailReceiverList = new ArrayList<>();
			emailReceiverList.add(userInfo.getEmail());
			if (inviteUserResponse.isMemberUser()) {
				Context context = mailContextHandler.getWorkspaceInviteContext(
					sessionCode, locale, workspace.getName(), userInfo,
					inviteUserResponse.getInviteUserDetailInfo(),
					masterUser
				);
				applicationEventPublisher.publishEvent(
					new MailSendEvent(context, Mail.WORKSPACE_INVITE, locale, emailReceiverList));
			} else {
				Context context = mailContextHandler.getWorkspaceInviteNonUserContext(
					sessionCode, locale, workspace.getName(), userInfo, masterUser);
				applicationEventPublisher.publishEvent(
					new MailSendEvent(context, Mail.WORKSPACE_INVITE_NON_USER, locale, emailReceiverList));
			}
		});
		return new ApiResponse<>(true);
	}

	/**
	 * 워크스페이스 초대 시 부여한 라이선스 검증
	 *
	 * @param userInfoList                     - 초대받은 유저 목록
	 * @param workspaceLicensePlanInfoResponse - 워크스페이스 라이선스 정보
	 */
	private void checkWorkspaceInviteLicenseProduct(
		List<WorkspaceInviteRequest.UserInfo> userInfoList,
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse
	) {
		long requestRemoteAmount = userInfoList.stream()
			.filter(WorkspaceInviteRequest.UserInfo::isPlanRemote)
			.count();
		long requestMakeAmount = userInfoList.stream().filter(WorkspaceInviteRequest.UserInfo::isPlanMake).count();
		long requestViewAmount = userInfoList.stream().filter(WorkspaceInviteRequest.UserInfo::isPlanView).count();
		log.info(
			"[WORKSPACE INVITE USER] Request license amount. remote : {}, make : {}, view : {}",
			requestRemoteAmount,
			requestMakeAmount, requestViewAmount
		);
		if (requestRemoteAmount > 0) {
			Integer unUsedRemoteAmount = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
				.stream()
				.filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals("REMOTE"))
				.map(WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse::getUnUseLicenseAmount)
				.findFirst()
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_NON_LICENSE));
			if (requestRemoteAmount > unUsedRemoteAmount) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_NON_LICENSE);
			}
		}
		if (requestMakeAmount > 0) {
			Integer unUsedMakeAmount = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
				.stream()
				.filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals("MAKE"))
				.map(WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse::getUnUseLicenseAmount)
				.findFirst()
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_NON_LICENSE));
			if (requestMakeAmount > unUsedMakeAmount) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_NON_LICENSE);
			}
		}
		if (requestViewAmount > 0) {
			Integer unUsedViewAmount = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
				.stream()
				.filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals("VIEW"))
				.map(WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse::getUnUseLicenseAmount)
				.findFirst()
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_NON_LICENSE));
			if (requestViewAmount > unUsedViewAmount) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_NON_LICENSE);
			}
		}
	}

	/**
	 * 워크스페이스 초대 시 초대받은 유저가 최대 소속 워크스페이스 수를 넘겼는지 체크
	 *
	 * @param inviteUserInfoResponseMap - 초대받은 유저 정보 목록
	 */
	private void checkWorkspaceInvitedUserMaxJoin(Map<String, InviteUserInfoResponse> inviteUserInfoResponseMap) {
		inviteUserInfoResponseMap.values().forEach(inviteUserInfoResponse -> {
			long userIncludedWorkspaceAmount = workspaceUserRepository.countByUserId(
				inviteUserInfoResponse.getInviteUserDetailInfo().getUserUUID());
			if (inviteUserInfoResponse.isMemberUser() && userIncludedWorkspaceAmount + 1 > MAX_JOIN_WORKSPACE_AMOUNT) {
				log.error(
					"[WORKSPACE INVITE USER] maximum join workspace amount : [{}], current user join workspace amount(include request) : [{}]",
					MAX_JOIN_WORKSPACE_AMOUNT, userIncludedWorkspaceAmount + 1
				);
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_MAX_JOIN_USER);
			}
		});
	}

	/**
	 * 워크스페이스 초대 시 초대받은 유저가 이미 해당 워크스페이스의 소속되어 있는지 체크
	 * 단, 비회원이 아닌 경우에만 체크한다.
	 *
	 * @param workspaceId               - 초대한 워크스페이스 식별자
	 * @param inviteUserInfoResponseMap - 초대받은 유저 정보 목록
	 */
	private void checkWorkspaceInvitedUserAlreadyJoin(
		String workspaceId, Map<String, InviteUserInfoResponse> inviteUserInfoResponseMap
	) {
		inviteUserInfoResponseMap.values().forEach(inviteUserInfoResponse -> {
			Optional<WorkspaceUser> invitedWorkspaceUser = workspaceUserRepository.findByUserIdAndWorkspace_Uuid(
				inviteUserInfoResponse.getInviteUserDetailInfo().getUserUUID(), workspaceId);
			if (inviteUserInfoResponse.isMemberUser() && invitedWorkspaceUser.isPresent()) {
				log.error(
					"[WORKSPACE INVITE USER] Invite User is already Workspace user. Invite user is Member >>> [{}], Invite user is Workspace User >>> [{}]",
					inviteUserInfoResponse.isMemberUser(), true
				);
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ALREADY_EXIST);
			}
		});

	}

	/**
	 * 워크스페이스 초대 시 탈퇴한 유저가 있는지 체크
	 *
	 * @param invitedUserEmailList - 워크스페이스 초대 유저 이메일 목록
	 * @return - 탈퇴한 유저가 없는 유저 정보 모록
	 */
	private Map<String, InviteUserInfoResponse> checkWorkspaceInvitedUserSecession
	(List<String> invitedUserEmailList) {
		Map<String, InviteUserInfoResponse> inviteUserInfoResponseMap = new HashMap<>();
		invitedUserEmailList.forEach(invitedUserEmail -> {
			ApiResponse<InviteUserInfoResponse> inviteUserInfoResponseApiResponse = userRestServiceHandler.getInviteUserRequest(
				invitedUserEmail);
			if (inviteUserInfoResponseApiResponse.getCode() != 200) {
				log.error("[WORKSPACE INVITE USER] Invalid Invited User Info.");
				if (inviteUserInfoResponseApiResponse.getCode() == 5002) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE_SECESSION_USER);
				}
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVITE);
			}
			InviteUserInfoResponse inviteUserResponse = inviteUserInfoResponseApiResponse.getData();
			inviteUserInfoResponseMap.put(invitedUserEmail, inviteUserResponse);
		});
		return inviteUserInfoResponseMap;
	}

	/**
	 * 워크스페이스 초대 요청 유저의 권한 유효성 체크
	 *
	 * @param workspaceId   - 워크스페이스 식별자
	 * @param requestUserId - 초대 요청 유저 식별자
	 * @param userInfoList  - 초대 대상 유저 역할 리스트
	 */
	private void checkWorkspaceInvitePermission(
		String workspaceId, String requestUserId, List<WorkspaceInviteRequest.UserInfo> userInfoList
	) {
		List<Role> invitedUserRoleList = userInfoList.stream()
			.map(userInfo -> Role.valueOf(userInfo.getRole()))
			.collect(Collectors.toList());
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, requestUserId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION));

		// 초대한 사람이 마스터 또는 매니저여야 한다.
		if (!isMasterOrManagerRole(requestUserPermission.getWorkspaceRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
		// 매니저 유저는 매니저 유저를 초대할 수 없다.
		if (requestUserPermission.getWorkspaceRole().getRole() == Role.MANAGER && invitedUserRoleList.stream()
			.anyMatch(s -> s == Role.MANAGER)) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
	}

	/**
	 * 워크스페이스 초대 수락
	 *
	 * @param sessionCode - 초대 세션 식별자
	 * @param lang        - 사용자 언어
	 * @return - 리다이렉트 url
	 */
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public RedirectView inviteWorkspaceAccept(String sessionCode, String lang) {
		Locale locale = getLocale(lang);

		//1-1. 초대 세션 유효성 체크
		Optional<UserInvite> optionalUserInvite = userInviteRepository.findById(sessionCode);
		if (!optionalUserInvite.isPresent()) {
			log.error(
				"[WORKSPACE INVITE ACCEPT] Workspace invite session Info Not found. session code >> [{}]",
				sessionCode
			);
			return redirectView(redirectProperty.getConsoleWeb() + RedirectPath.WORKSPACE_INVITE_FAIL.getValue());
		}
		UserInvite userInvite = optionalUserInvite.get();
		log.info("[WORKSPACE INVITE ACCEPT] Workspace invite session Info >> [{}]", userInvite);

		//1-2. 초대받은 유저가 유효한지 체크
		ApiResponse<InviteUserInfoResponse> inviteUserInfoResponseApiResponse = userRestServiceHandler.getInviteUserRequest(
			userInvite.getInvitedUserEmail());
		if (inviteUserInfoResponseApiResponse.getCode() != 200) {
			log.error("[WORKSPACE INVITE ACCEPT] Invalid Invited User Info.");
			//탈퇴한 유저의 캐싱은 삭제, 이외의 유저는 보류.
			if (inviteUserInfoResponseApiResponse.getCode() == 5002) {
				applicationEventPublisher.publishEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()));
			}
			return redirectView(redirectProperty.getConsoleWeb() + RedirectPath.WORKSPACE_INVITE_FAIL.getValue());
		}

		InviteUserInfoResponse inviteUserResponse = inviteUserInfoResponseApiResponse.getData();
		//1-3. 초대받은 유저가 비회원인지 체크
		if (!inviteUserResponse.isMemberUser()) {
			log.info("[WORKSPACE INVITE ACCEPT] Invited User is Member >> [{}]", inviteUserResponse.isMemberUser());
			return redirectView(
				redirectProperty.getTermsWeb() + "?inviteSession=" + sessionCode + "&lang=" + lang + "&email="
					+ userInvite.getInvitedUserEmail());
		}

		//1-4. 비회원인경우 초대 session정보에 uuid가 null값이므로 회원가입 후에 수락하게 하고 user서버에서 회원정보를 가져온다.(회원가입후에 수락으로 리다이렉트한 경우 이 과정이 필요하다.)
		InviteUserDetailInfoResponse inviteUserInfo = inviteUserResponse.getInviteUserDetailInfo();
		userInvite.setInvitedUserEmail(inviteUserInfo.getEmail());
		userInvite.setInvitedUserId(inviteUserInfo.getUserUUID());
		userInviteRepository.save(userInvite);

		Workspace workspace = workspaceRepository.findByUuid(userInvite.getWorkspaceId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(workspace.getUserId());
		if (masterUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}

		//1-5. 유저가 최대 참여 가능한 워크스페이스 수 체크
		long maxJoinWorkspaceAmount = workspaceUserRepository.countByWorkspace_Uuid(workspace.getUuid());
		if (maxJoinWorkspaceAmount + 1 > MAX_JOIN_WORKSPACE_AMOUNT) {
			log.error(
				"[WORKSPACE INVITE ACCEPT] Over Max Workspace join amount. max join workspace Amount >> [{}], current workspace joined amount(include master user) >> [{}]"
				, MAX_JOIN_WORKSPACE_AMOUNT, maxJoinWorkspaceAmount);

			WorkspaceInviteProcess workspaceInviteProcess = WorkspaceInviteProcess.builder()
				.applicationEventPublisher(applicationEventPublisher)
				.inviteSessionDeleteEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()))
				.mailSendEvent(new MailSendEvent(
					mailContextHandler.getWorkspaceOverJoinContext(
						workspace.getName(), masterUserInfo, inviteUserInfo, userInvite.isPlanRemote(),
						userInvite.isPlanMake(), userInvite.isPlanView()
					),
					Mail.WORKSPACE_OVER_JOIN_FAIL,
					locale,
					getMasterAndManagerEmail(workspace, masterUserInfo)
				))
				.redirectUrl(
					redirectProperty.getWorkstationWeb() + RedirectPath.WORKSPACE_OVER_JOIN_FAIL.getValue())
				.build();
			return workspaceInviteProcess.process();
		}

		//1-6. 워크스페이스에서 최대 참여 가능한 멤버 수 체크
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = getWorkspaceLicensesByWorkspaceId(
			workspace.getUuid());
		try {
			checkWorkspaceMaxUserAmount(workspace.getUuid(), 1, workspaceLicensePlanInfoResponse);
		} catch (WorkspaceException e) {
			WorkspaceInviteProcess workspaceInviteProcess = WorkspaceInviteProcess.builder()
				.applicationEventPublisher(applicationEventPublisher)
				.inviteSessionDeleteEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()))
				.mailSendEvent(new MailSendEvent(
					mailContextHandler.getWorkspaceOverMaxUserContext(
						workspace.getName(), masterUserInfo, inviteUserInfo, userInvite.isPlanRemote(),
						userInvite.isPlanMake(), userInvite.isPlanView()
					),
					Mail.WORKSPACE_OVER_MAX_USER_FAIL,
					locale,
					getMasterAndManagerEmail(workspace, masterUserInfo)
				))
				.redirectUrl(
					redirectProperty.getWorkstationWeb() + RedirectPath.WORKSPACE_OVER_MAX_USER_FAIL.getValue())
				.build();
			return workspaceInviteProcess.process();
		}
		//2. 초대 수락 수행 - 플랜 할당. 할당에 실패하면 초대 로직 원복.
		List<String> requestPlanList = getRequestWorkspaceLicense(
			userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView());
		if (!requestPlanList.isEmpty()) {
			List<String> failPlanList = executeGrantWorkspaceLicenseToUser(
				workspace.getUuid(), inviteUserInfo.getUserUUID(), requestPlanList);
			if (!failPlanList.isEmpty()) {
				requestPlanList.removeAll(failPlanList);
				requestPlanList.forEach(
					productName -> revokeWorkspaceLicenseToUser(workspace.getUuid(), inviteUserInfo.getUserUUID(),
						productName
					));
				WorkspaceInviteProcess workspaceInviteAcceptProcess = WorkspaceInviteProcess.builder()
					.applicationEventPublisher(applicationEventPublisher)
					.inviteSessionDeleteEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()))
					.mailSendEvent(new MailSendEvent(
						mailContextHandler.getWorkspaceOverPlanContext(
							workspace.getName(), masterUserInfo, inviteUserInfo, requestPlanList, failPlanList),
						Mail.WORKSPACE_OVER_PLAN_FAIL,
						locale,
						new ArrayList<>()
					))
					.redirectUrl(
						redirectProperty.getWorkstationWeb() + RedirectPath.WORKSPACE_OVER_PLAN_FAIL.getValue())
					.build();
				return workspaceInviteAcceptProcess.process();
			}
		}

		if (workspaceUserRepository.findByUserIdAndWorkspace_Uuid(
			userInvite.getInvitedUserId(), userInvite.getWorkspaceId()).isPresent()) {
			log.error("[WORKSPACE INVITE ACCEPT] Already join");
			return redirectView(redirectProperty.getWorkstationWeb());
		} else {
			//워크스페이스 소속 넣기 (workspace_user)
			WorkspaceUser workspaceUser = WorkspaceUser.builder()
				.workspace(workspace)
				.userId(userInvite.getInvitedUserId())
				.build();
			workspaceUserRepository.save(workspaceUser);

			//워크스페이스 권한 부여하기 (workspace_user_permission)
			WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(Role.valueOf(userInvite.getRole()))
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
			WorkspacePermission workspacePermission = workspacePermissionRepository.findByPermission(Permission.ALL)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
			WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
				.workspaceUser(workspaceUser)
				.workspaceRole(workspaceRole)
				.workspacePermission(workspacePermission)
				.build();
			workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

			log.info("[WORKSPACE INVITE ACCEPT] Workspace user save");
		}

		String message;
		if (Role.valueOf(userInvite.getRole()) == Role.MANAGER) {
			message = messageSource.getMessage(
				"WORKSPACE_INVITE_MANAGER",
				new String[] {inviteUserInfo.getNickname(), String.join(",", requestPlanList)}, locale
			);
		} else {
			message = messageSource.getMessage(
				"WORKSPACE_INVITE_MEMBER",
				new String[] {inviteUserInfo.getNickname(), String.join(",", requestPlanList)}, locale
			);
		}
		// 수락 성공 프로세스
		WorkspaceInviteProcess workspaceInviteAcceptProcess = WorkspaceInviteProcess.builder()
			.applicationEventPublisher(applicationEventPublisher)
			.historyAddEvent(new HistoryAddEvent(message, userInvite.getInvitedUserId(), workspace))
			.mailSendEvent(new MailSendEvent(
				mailContextHandler.getWorkspaceInviteAcceptContext(
					workspace.getName(), masterUserInfo, inviteUserInfo, userInvite.getRole(),
					userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView()
				),
				Mail.WORKSPACE_INVITE_ACCEPT,
				locale,
				getMasterAndManagerEmail(workspace, masterUserInfo)
			))
			.redirectUrl(redirectProperty.getWorkstationWeb())
			.inviteSessionDeleteEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()))
			.build();
		return workspaceInviteAcceptProcess.process();
	}

	private Locale getLocale(String lang) {
		Locale locale = Locale.KOREAN;
		if (StringUtils.hasText(lang)) {
			locale = new Locale(lang, "");
		}
		return locale;
	}

	private RedirectView redirectView(String url) {
		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(url);
		redirectView.setContentType("application/json");
		return redirectView;
	}

	private List<String> executeGrantWorkspaceLicenseToUser(
		String workspaceId, String
		inviteUserId, List<String> requestPlanList
	) {
		List<String> failPlanList = new ArrayList<>();
		requestPlanList.forEach(requestPlan -> {
			try {
				grantWorkspaceLicenseToUser(workspaceId, inviteUserId, requestPlan);
			} catch (WorkspaceException e) {
				failPlanList.add(requestPlan);
			}
		});
		return failPlanList;
	}

	private List<String> getRequestWorkspaceLicense(boolean planRemote, boolean planMake, boolean planView) {
		List<String> result = new ArrayList<>();
		if (planRemote) {
			result.add("REMOTE");
		}
		if (planMake) {
			result.add("MAKE");
		}
		if (planView) {
			result.add("VIEW");
		}
		return result;
	}

	private List<String> getMasterAndManagerEmail(Workspace workspace, UserInfoRestResponse masterUserInfo) {
		List<String> emailReceiverList = new ArrayList<>();
		List<WorkspaceUserPermission> managerUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(
			workspace, Role.MANAGER);
		if (managerUserPermissionList != null && !managerUserPermissionList.isEmpty()) {
			managerUserPermissionList.forEach(workspaceUserPermission -> {
				UserInfoRestResponse managerUserInfo = userRestServiceHandler.getUserRequest(
					workspaceUserPermission.getWorkspaceUser().getUserId());
				if (managerUserInfo.isEmtpy()) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
				}
				emailReceiverList.add(managerUserInfo.getEmail());
			});
		}
		emailReceiverList.add(masterUserInfo.getEmail());
		return emailReceiverList;
	}

	public RedirectView inviteWorkspaceReject(String sessionCode, String lang) {
		Locale locale = getLocale(lang);

		Optional<UserInvite> optionalUserInvite = userInviteRepository.findById(sessionCode);
		if (!optionalUserInvite.isPresent()) {
			log.info(
				"[WORKSPACE INVITE REJECT] Workspace invite session Info Not found. session code >> [{}]",
				sessionCode
			);
			return redirectView(redirectProperty.getWorkstationWeb());
		}
		UserInvite userInvite = optionalUserInvite.get();
		log.info("[WORKSPACE INVITE REJECT] Workspace Invite Session Info >> [{}] ", userInvite);

		//비회원 거절은 메일 전송 안함.
		ApiResponse<InviteUserInfoResponse> inviteUserInfoResponseApiResponse = userRestServiceHandler.getInviteUserRequest(
			userInvite.getInvitedUserEmail());
		if (inviteUserInfoResponseApiResponse.getCode() != 200) {
			log.error("[WORKSPACE INVITE REJECT] Invalid Invited User Info.");
			//탈퇴한 유저의 캐싱은 삭제, 이외의 유저는 보류.
			if (inviteUserInfoResponseApiResponse.getCode() == 5002) {
				applicationEventPublisher.publishEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()));
			}
			return redirectView(redirectProperty.getWorkstationWeb());
		}
		InviteUserInfoResponse inviteUserResponse = inviteUserInfoResponseApiResponse.getData();

		//비회원
		if (!inviteUserResponse.isMemberUser()) {
			log.info("[WORKSPACE INVITE REJECT] Invited User is Member >> [{}]", inviteUserResponse.isMemberUser());
			applicationEventPublisher.publishEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()));
			return redirectView(redirectProperty.getWorkstationWeb());
		}
		//비회원일경우 초대 session정보에 uuid가 안들어가므로 user서버에서 조회해서 가져온다.
		InviteUserDetailInfoResponse inviteUserInfo = inviteUserResponse.getInviteUserDetailInfo();
		userInvite.setInvitedUserEmail(inviteUserInfo.getEmail());
		userInvite.setInvitedUserId(inviteUserInfo.getUserUUID());
		userInviteRepository.save(userInvite);

		//MAIL 발송
		Workspace workspace = workspaceRepository.findByUuid(userInvite.getWorkspaceId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(workspace.getUserId());
		if (masterUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}

		List<String> emailReceiverList = getMasterAndManagerEmail(workspace, masterUserInfo);

		WorkspaceInviteProcess workspaceInviteProcess = WorkspaceInviteProcess.builder()
			.applicationEventPublisher(applicationEventPublisher)
			.mailSendEvent(new MailSendEvent(
				mailContextHandler.getWorkspaceInviteRejectContext(workspace.getName(), inviteUserInfo),
				Mail.WORKSPACE_INVITE_REJECT, locale, emailReceiverList
			))
			.redirectUrl(redirectProperty.getWorkstationWeb())
			.inviteSessionDeleteEvent(new InviteSessionDeleteEvent(userInvite.getSessionCode()))
			.build();
		return workspaceInviteProcess.process();
	}

	@Override
	public boolean deleteWorkspaceMemberAccount(
		String workspaceId, MemberAccountDeleteRequest memberAccountDeleteRequest
	) {
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
		/*UserInfoRestResponse masterUserInfo = userRestServiceHandler.getUserRequest(workspace.getUserId());
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
		}*/

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
