package com.virnect.workspace.application.workspaceuser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.application.license.LicenseRestService;
import com.virnect.workspace.application.license.dto.LicenseRevokeResponse;
import com.virnect.workspace.application.license.dto.MyLicenseInfoListResponse;
import com.virnect.workspace.application.license.dto.MyLicenseInfoResponse;
import com.virnect.workspace.application.license.dto.WorkspaceLicensePlanInfoResponse;
import com.virnect.workspace.application.remote.RemoteRestService;
import com.virnect.workspace.application.user.UserRestServiceHandler;
import com.virnect.workspace.application.user.dto.request.GuestMemberRegistrationRequest;
import com.virnect.workspace.application.user.dto.request.MemberRegistrationRequest;
import com.virnect.workspace.application.user.dto.request.MemberUserPasswordChangeRequest;
import com.virnect.workspace.application.user.dto.response.MemberUserPasswordChangeResponse;
import com.virnect.workspace.application.user.dto.response.UserInfoListRestResponse;
import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.application.user.dto.response.UserProfileUpdateResponse;
import com.virnect.workspace.dao.workspace.WorkspaceRepository;
import com.virnect.workspace.dao.workspacepermission.WorkspacePermissionRepository;
import com.virnect.workspace.dao.workspacerole.WorkspaceRoleRepository;
import com.virnect.workspace.dao.workspaceuser.WorkspaceUserRepository;
import com.virnect.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.workspace.domain.rest.LicenseStatus;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.UserType;
import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspacePermission;
import com.virnect.workspace.domain.workspace.WorkspaceRole;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.workspace.dto.request.MemberAccountCreateInfo;
import com.virnect.workspace.dto.request.MemberAccountCreateRequest;
import com.virnect.workspace.dto.request.MemberAccountDeleteRequest;
import com.virnect.workspace.dto.request.MemberGuestCreateRequest;
import com.virnect.workspace.dto.request.MemberGuestDeleteRequest;
import com.virnect.workspace.dto.request.MemberKickOutRequest;
import com.virnect.workspace.dto.request.MemberProfileUpdateRequest;
import com.virnect.workspace.dto.request.MemberUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.workspace.dto.request.WorkspaceMemberPasswordChangeRequest;
import com.virnect.workspace.dto.response.MemberProfileUpdateResponse;
import com.virnect.workspace.dto.response.MemberSeatDeleteResponse;
import com.virnect.workspace.dto.response.PageMetadataRestResponse;
import com.virnect.workspace.dto.response.WorkspaceMemberInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceMemberPasswordChangeResponse;
import com.virnect.workspace.dto.response.WorkspaceNewMemberInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserLicenseInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserLicenseListResponse;
import com.virnect.workspace.event.cache.UserWorkspacesDeleteEvent;
import com.virnect.workspace.event.history.HistoryAddEvent;
import com.virnect.workspace.event.message.MailContextHandler;
import com.virnect.workspace.event.message.MailSendEvent;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.CustomPageHandler;
import com.virnect.workspace.global.common.CustomPageResponse;
import com.virnect.workspace.global.common.mapper.rest.RestMapStruct;
import com.virnect.workspace.global.constant.Mail;
import com.virnect.workspace.global.constant.Permission;
import com.virnect.workspace.global.error.ErrorCode;

/**
 * Project: PF-Workspace
 * DATE: 2021-05-13
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public abstract class WorkspaceUserService {
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceUserRepository workspaceUserRepository;
	private final WorkspaceRoleRepository workspaceRoleRepository;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final MessageSource messageSource;
	private final LicenseRestService licenseRestService;
	private final RestMapStruct restMapStruct;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final MailContextHandler mailContextHandler;
	private final WorkspacePermissionRepository workspacePermissionRepository;
	private final RemoteRestService remoteRestService;
	private final UserRestServiceHandler userRestServiceHandler;

	private static final int MAX_WORKSPACE_USER_AMOUNT = 50;//워크스페이스 최대 멤버 수(마스터 본인 포함)

	/**
	 * 멤버 조회
	 *
	 * @param workspaceId - 조회 대상 워크스페이스 식별자
	 * @param search      - 검색 필터링
	 * @param filter      - 조건 필터링
	 * @param roleFilter
	 * @param pageRequest - 페이징 정보
	 * @return - 워크스페이스 소속 멤버 목록
	 */

	public WorkspaceUserInfoListResponse getMembers(
		String workspaceId, String search, String filter, List<Role> roleFilter, String userTypeFilter,
		String planFilter, com.virnect.workspace.global.common.PageRequest pageRequest, boolean paging
	) {
		//1. 정렬 검증으로 Pageable 재정의
		Pageable newPageable = getPageable(pageRequest);

		//2. search 필터링
		List<String> resultUserIdList = workspaceUserRepository.getWorkspaceUserIdList(workspaceId);//워크스페이스 소속 전체 유저
		if (StringUtils.hasText(search)) {
			UserInfoListRestResponse userInfoListRestResponse = userRestServiceHandler.getUserListRequest(
				search, resultUserIdList);
			resultUserIdList = userInfoListRestResponse.getUserInfoList()
				.stream()
				.map(UserInfoRestResponse::getUuid)
				.collect(Collectors.toList());
		}

		//3. 필터링
		if (StringUtils.hasText(filter)) {
			//3-1. 라이선스 플랜으로 필터링
			if (filter.matches(".*(?i)REMOTE.*|.*(?i)MAKE.*|.*(?i)VIEW.*") && !resultUserIdList.isEmpty()) {
				resultUserIdList = filterUserIdListByPlan(workspaceId, resultUserIdList, filter);
			}
			//3-2. 워크스페이스 역할로 필터링
			else if (filter.matches(".*(?i)MASTER.*|.*(?i)MANAGER.*|.*(?i)MEMBER.*|.*(?i)GUEST.*")
				&& !resultUserIdList.isEmpty()) {
				String[] filters = filter.toUpperCase().split(",").length == 0 ? new String[] {filter.toUpperCase()} :
					filter.toUpperCase().split(",");
				List<Role> roleList = new ArrayList<>();
				Arrays.stream(Role.values()).forEach(role -> {
					for (String StringRole : filters) {
						if (role.name().equals(StringRole)) {
							roleList.add(role);
						}
					}
				});
				resultUserIdList = workspaceUserPermissionRepository.getUserIdsByInUserListAndEqRole(
					resultUserIdList, roleList, workspaceId);
			}
		}
		//3-3. 워크스페이스 역할로 필터링
		if (!CollectionUtils.isEmpty(roleFilter)) {
			resultUserIdList = workspaceUserPermissionRepository.getUserIdsByInUserListAndEqRole(
				resultUserIdList, roleFilter, workspaceId);
		}

		//3-4. 유저 타입으로 필터링
		if (StringUtils.hasText(userTypeFilter)) {
			UserInfoListRestResponse userInfoListRestResponse = userRestServiceHandler.getUserListRequest(
				null, resultUserIdList);
			String[] userTypes =
				userTypeFilter.toUpperCase().split(",").length == 0 ? new String[] {userTypeFilter.toUpperCase()} :
					userTypeFilter.toUpperCase().split(",");
			List<String> filteredUserIdList = new ArrayList<>();
			userInfoListRestResponse.getUserInfoList().forEach(userInfoRestResponse -> {
				if (Arrays.stream(userTypes).anyMatch(s -> s.equals(userInfoRestResponse.getUserType().name()))) {
					filteredUserIdList.add(userInfoRestResponse.getUuid());
				}
			});
			resultUserIdList = filteredUserIdList;
		}

		//3-5. 라이선스 플랜으로 필터링
		if (StringUtils.hasText(planFilter)) {
			resultUserIdList = filterUserIdListByPlan(workspaceId, resultUserIdList, planFilter);
		}

		//4. 결과가 없는 경우에는 빠른 리턴
		if (resultUserIdList.isEmpty()) {
			PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
			pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
			pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());
			return new WorkspaceUserInfoListResponse();
		}

		//5-1. 최종적으로 필터링된 유저들을 조회한다.
		if (!paging) {
			List<WorkspaceUserPermission> workspaceUserPermissionList = workspaceUserPermissionRepository.getWorkspaceUserListByInUserList(
				resultUserIdList, workspaceId);
			List<WorkspaceUserInfoResponse> workspaceUserListResponse = generateWorkspaceUserListResponse(
				workspaceUserPermissionList);
			return new WorkspaceUserInfoListResponse(workspaceUserListResponse);
		}

		//5-2. 최종적으로 필터링된 유저들을 페이징한다.
		Page<WorkspaceUserPermission> workspaceUserPermissionPage = workspaceUserPermissionRepository.getWorkspaceUserPageByInUserList(
			resultUserIdList, newPageable, workspaceId);
		List<WorkspaceUserInfoResponse> workspaceUserListResponse = generateWorkspaceUserListResponse(
			workspaceUserPermissionPage.toList());

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(workspaceUserPermissionPage.getTotalElements());
		pageMetadataResponse.setTotalPage(workspaceUserPermissionPage.getTotalPages());
		pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
		pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

		//6. email, nickname으로 sort 요청한 경우 결과 리스트 내에서 다시 sorting.
		if (pageRequest.getSortName().equalsIgnoreCase("email") || pageRequest.getSortName()
			.equalsIgnoreCase("nickname")) {
			return new WorkspaceUserInfoListResponse(
				getSortedMemberList(pageRequest, workspaceUserListResponse), pageMetadataResponse);
		}
		return new WorkspaceUserInfoListResponse(workspaceUserListResponse, pageMetadataResponse);

	}

	private List<WorkspaceUserInfoResponse> generateWorkspaceUserListResponse(
		List<WorkspaceUserPermission> workspaceUserPermissionList
	) {
		List<WorkspaceUserInfoResponse> workspaceUserInfoResponseList = new ArrayList<>();
		workspaceUserPermissionList.forEach(workspaceUserPermission -> {
			//1. REST - USER_SERVER : 유저 정보 조회
			UserInfoRestResponse userInfoResponse = userRestServiceHandler.getUserRequest(
				workspaceUserPermission.getWorkspaceUser().getUserId());
			if (!userInfoResponse.isEmtpy()) {
				WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
					userInfoResponse);
				workspaceUserInfoResponse.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
				workspaceUserInfoResponse.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());
				workspaceUserInfoResponse.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());
				//2. REST - LICENSE_SERVER : 유저의 라이선스 정보 조회
				String[] userLicenseProducts = getUserLicenseProducts(
					workspaceUserPermission.getWorkspaceUser().getWorkspace().getUuid(),
					workspaceUserPermission.getWorkspaceUser().getUserId()
				);
				workspaceUserInfoResponse.setLicenseProducts(userLicenseProducts);
				workspaceUserInfoResponseList.add(workspaceUserInfoResponse);
			}
		});
		return workspaceUserInfoResponseList;
	}

	private String[] getUserLicenseProducts(String workspaceId, String userId) {
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(workspaceId, userId);
		return myLicenseInfoListResponse.getLicenseInfoList().isEmpty() ? new String[0]
			: myLicenseInfoListResponse.getLicenseInfoList()
			.stream()
			.map(MyLicenseInfoResponse::getProductName)
			.toArray(String[]::new);
	}

	private List<String> filterUserIdListByPlan(String workspaceId, List<String> userIdList, String planFilter) {
		List<String> filterdUserIdList = new ArrayList<>();
		for (String userId : userIdList) {
			MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(workspaceId, userId);
			List<MyLicenseInfoResponse> myLicenseInfoList = myLicenseInfoListResponse.getLicenseInfoList();
			if (!CollectionUtils.isEmpty(myLicenseInfoList)) {
				if (myLicenseInfoListResponse.getLicenseInfoList()
					.stream()
					.anyMatch(myLicenseInfoResponse -> planFilter.toUpperCase()
						.contains(myLicenseInfoResponse.getProductName().toUpperCase()))) {
					filterdUserIdList.add(userId);
				}
			}
		}
		return filterdUserIdList;
	}

	private Pageable getPageable(com.virnect.workspace.global.common.PageRequest pageRequest) {
		//정렬을 빼고 Pageable 객체를 만든다.
		Pageable pageable = PageRequest.of(pageRequest.of().getPageNumber(), pageRequest.of().getPageSize());
		//정렬요청이 없는 경우에는 worksapceUser.updateDate,desc을 기본값으로 정렬하는 것으로 Pageable 객체를 수정한다.
		if (pageRequest.getSortName().equals("updatedDate")) {
			pageRequest.setSort("workspaceUser.updatedDate,desc");
			pageable = pageRequest.of();
		}
		//워크스페이스에서 정렬이 가능한 경우에는 Pageable 객체를 수정한다.
		if (pageRequest.getSortName().equalsIgnoreCase("role") || pageRequest.getSortName()
			.equalsIgnoreCase(("joinDate"))) {
			pageable = pageRequest.of();
		}
		return pageable;
	}

	MyLicenseInfoListResponse getMyLicenseInfoRequestHandler(String workspaceId, String userId) {
		ApiResponse<MyLicenseInfoListResponse> apiResponse = licenseRestService.getMyLicenseInfoRequestHandler(
			workspaceId, userId);
		if (apiResponse.getCode() != 200) {
			log.error(
				"[GET MY LICENSE INFO BY WORKSPACE UUID & USER UUID] request workspaceId : {}, request userId : {}, response code : {}",
				workspaceId, userId, apiResponse.getCode()
			);
			MyLicenseInfoListResponse myLicenseInfoListResponse = new MyLicenseInfoListResponse();
			myLicenseInfoListResponse.setLicenseInfoList(new ArrayList<>());
			return myLicenseInfoListResponse;
		}
		return apiResponse.getData();
	}

	public List<WorkspaceUserInfoResponse> getSortedMemberList(
		com.virnect.workspace.global.common.PageRequest
			pageRequest, List<WorkspaceUserInfoResponse> workspaceUserInfoResponseList
	) {
		String sortName = pageRequest.getSortName();
		String sortDirection = pageRequest.getSortDirection();
		if (sortName.equalsIgnoreCase("email") && sortDirection.equalsIgnoreCase("asc")) {
			return workspaceUserInfoResponseList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserInfoResponse::getEmail,
					Comparator.nullsFirst(Comparator.naturalOrder())
				))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("email") && sortDirection.equalsIgnoreCase("desc")) {
			return workspaceUserInfoResponseList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserInfoResponse::getEmail,
					Comparator.nullsFirst(Comparator.reverseOrder())
				))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("nickname") && sortDirection.equalsIgnoreCase("asc")) {
			List<WorkspaceUserInfoResponse> koList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> org.apache.commons.lang.StringUtils.left(memberInfoDTO.getNickName(), 1)
					.matches("[가-힣\\s]"))
				.sorted(Comparator.comparing(WorkspaceUserInfoResponse::getNickName))
				.collect(Collectors.toList());
			List<WorkspaceUserInfoResponse> enList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> org.apache.commons.lang.StringUtils.left(memberInfoDTO.getNickName(), 1)
					.matches("[a-zA-Z\\s]"))
				.sorted(Comparator.comparing(WorkspaceUserInfoResponse::getNickName))
				.collect(Collectors.toList());
			List<WorkspaceUserInfoResponse> etcList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> !koList.contains(memberInfoDTO))
				.filter(memberInfoDTO -> !enList.contains(memberInfoDTO))
				.sorted(Comparator.comparing(WorkspaceUserInfoResponse::getNickName))
				.collect(Collectors.toList());
			List<WorkspaceUserInfoResponse> nullList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> !StringUtils.hasText(memberInfoDTO.getNickName()))
				.collect(Collectors.toList());
			enList.addAll(etcList);
			koList.addAll(enList);
			nullList.addAll(koList);
			return nullList;
		}
		if (sortName.equalsIgnoreCase("nickname") && sortDirection.equalsIgnoreCase("desc")) {
			List<WorkspaceUserInfoResponse> koList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> org.apache.commons.lang.StringUtils.left(memberInfoDTO.getNickName(), 1)
					.matches("[가-힣\\s]"))
				.sorted(Comparator.comparing(WorkspaceUserInfoResponse::getNickName).reversed())
				.collect(Collectors.toList());
			List<WorkspaceUserInfoResponse> enList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> org.apache.commons.lang.StringUtils.left(memberInfoDTO.getNickName(), 1)
					.matches("[a-zA-Z\\s]"))
				.sorted(Comparator.comparing(WorkspaceUserInfoResponse::getNickName).reversed())
				.collect(Collectors.toList());
			List<WorkspaceUserInfoResponse> etcList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> !koList.contains(memberInfoDTO))
				.filter(memberInfoDTO -> !enList.contains(memberInfoDTO))
				.sorted(Comparator.comparing(WorkspaceUserInfoResponse::getNickName).reversed())
				.collect(Collectors.toList());
			List<WorkspaceUserInfoResponse> nullList = workspaceUserInfoResponseList.stream()
				.filter(memberInfoDTO -> !StringUtils.hasText(memberInfoDTO.getNickName()))
				.collect(Collectors.toList());
			enList.addAll(etcList);
			koList.addAll(enList);
			nullList.addAll(koList);
			return nullList;
		} else {
			return workspaceUserInfoResponseList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserInfoResponse::getUpdatedDate,
					Comparator.nullsFirst(Comparator.reverseOrder())
				))
				.collect(Collectors.toList());
		}
	}

	/**
	 * 워크스페이스에 소속된 유저가 보유한 제품 라이선스 목록 조회
	 *
	 * @param workspaceId - 워크스페이스 식별자
	 * @param userId      - 유저 식별자
	 * @return - 제품 라이선스 목록
	 */
	public String[] getUserLicenseProductList(String workspaceId, String userId) {
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(workspaceId, userId);

		if (!CollectionUtils.isEmpty(myLicenseInfoListResponse.getLicenseInfoList())) {
			return myLicenseInfoListResponse.getLicenseInfoList()
				.stream()
				.map(MyLicenseInfoResponse::getProductName)
				.toArray(String[]::new);
		}
		return new String[0];
	}

	public List<WorkspaceNewMemberInfoResponse> getWorkspaceNewUserInfo(String workspaceId) {
		List<WorkspaceUserPermission> workspaceUserPermissionList = workspaceUserPermissionRepository.findRecentWorkspaceUserList(
			4, workspaceId);

		return workspaceUserPermissionList.stream().map(workspaceUserPermission -> {
			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(
				workspaceUserPermission.getWorkspaceUser().getUserId());
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			WorkspaceNewMemberInfoResponse newMemberInfo = restMapStruct.userInfoRestResponseToWorkspaceNewMemberInfoResponse(
				userInfoRestResponse);
			newMemberInfo.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());
			newMemberInfo.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
			return newMemberInfo;
		}).collect(Collectors.toList());
	}

	@Transactional
	public abstract ApiResponse<Boolean> reviseMemberInfo(
		String workspaceId, MemberUpdateRequest memberUpdateRequest, Locale locale
	);

	public void saveUserRoleUpdateHistory(
		WorkspaceRole updateWorkspaceRole, UserInfoRestResponse masterUserInfo, UserInfoRestResponse updateUserInfo,
		String requestUserId, Locale locale, Workspace workspace
	) {
		if (updateWorkspaceRole.getRole() == Role.MANAGER) {
			String message = messageSource.getMessage(
				"WORKSPACE_SET_MANAGER", new String[] {masterUserInfo.getNickname(), updateUserInfo.getNickname()},
				locale
			);
			applicationEventPublisher.publishEvent(
				new HistoryAddEvent(message, requestUserId,
					workspace
				));
		}
		if (updateWorkspaceRole.getRole() == Role.MEMBER) {
			String message = messageSource.getMessage(
				"WORKSPACE_SET_MEMBER", new String[] {masterUserInfo.getNickname(), updateUserInfo.getNickname()},
				locale
			);
			applicationEventPublisher.publishEvent(
				new HistoryAddEvent(message, requestUserId,
					workspace
				));
		}
	}

	public void saveUserLicenseUpdateHistory(
		List<String> addedProductList, List<String> removedProductList, UserInfoRestResponse requestUserInfo,
		UserInfoRestResponse updateUserInfo, Locale locale, Workspace workspace
	) {
		if (!addedProductList.isEmpty()) {
			String message = messageSource.getMessage(
				"WORKSPACE_GRANT_LICENSE",
				new String[] {requestUserInfo.getNickname(), updateUserInfo.getNickname(),
					org.apache.commons.lang.StringUtils.join(addedProductList, ",")}, locale
			);
			applicationEventPublisher.publishEvent(new HistoryAddEvent(message, updateUserInfo.getUuid(), workspace));
		}
		if (!removedProductList.isEmpty()) {
			String message = messageSource.getMessage(
				"WORKSPACE_REVOKE_LICENSE", new String[] {requestUserInfo.getNickname(), updateUserInfo.getNickname(),
					org.apache.commons.lang.StringUtils.join(removedProductList, ",")}, locale);
			applicationEventPublisher.publishEvent(new HistoryAddEvent(message, updateUserInfo.getUuid(), workspace));
		}
	}

	MyLicenseInfoResponse grantWorkspaceLicenseToUser(String workspaceId, String userId, String productName) {
		ApiResponse<MyLicenseInfoResponse> apiResponse = licenseRestService.grantWorkspaceLicenseToUser(
			workspaceId, userId, productName);
		if (apiResponse.getCode() != 200 || apiResponse.getData() == null || !StringUtils.hasText(
			apiResponse.getData().getProductName())) {
			log.error(
				"[GRANT WORKSPACE LICENSE TO USER] License grant fail. workspace uuid : {}, user uuid : {}, product name : {}, response code : {}, response message : {}",
				workspaceId, userId, productName, apiResponse.getCode(), apiResponse.getMessage()
			);
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
		} else {
			log.info(
				"[GRANT WORKSPACE LICENSE TO USER] License grant success. workspace uuid : {}, user uuid : {}, product name : {}",
				workspaceId, userId, productName
			);
			return apiResponse.getData();
		}
	}

	void revokeWorkspaceLicenseToUser(String workspaceId, String userId, String productName) {
		ApiResponse<LicenseRevokeResponse> apiResponse = licenseRestService.revokeWorkspaceLicenseToUser(
			workspaceId, userId, productName);
		if (apiResponse.getCode() != 200 || !apiResponse.getData().isResult()) {
			log.error(
				"[REVOKE WORKSPACE LICENSE TO USER] License revoke fail. workspace uuid : {}, user uuid : {}, product name : {}, response code : {}, response message : {}",
				workspaceId, userId, productName, apiResponse.getCode(), apiResponse.getMessage()
			);
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
		} else {
			log.info(
				"[REVOKE WORKSPACE LICENSE TO USER ] License revoke success. workspace uuid : {}, user uuid : {}, product name : {}",
				workspaceId, userId, productName
			);
		}
	}

	public WorkspaceUserInfoResponse getMemberInfo(String workspaceId, String userId) {
		Optional<WorkspaceUserPermission> workspaceUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
			workspaceId, userId);
		if (workspaceUserPermission.isPresent()) {
			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(userId);
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
			}
			WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
				userInfoRestResponse);
			workspaceUserInfoResponse.setRole(workspaceUserPermission.get().getWorkspaceRole().getRole());
			workspaceUserInfoResponse.setLicenseProducts(getUserLicenseProductList(workspaceId, userId));
			return workspaceUserInfoResponse;
		} else {
			return new WorkspaceUserInfoResponse();
		}
	}

	public WorkspaceUserInfoListResponse getMemberInfoList(String workspaceId, String[] userIds) {
		List<WorkspaceUserInfoResponse> workspaceUserInfoResponseList = new ArrayList<>();
		for (String userId : userIds) {
			Optional<WorkspaceUserPermission> workspaceUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, userId);
			if (workspaceUserPermission.isPresent()) {
				UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(userId);
				if (userInfoRestResponse.isEmtpy()) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
				}
				WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
					userInfoRestResponse);
				workspaceUserInfoResponse.setRole(workspaceUserPermission.get().getWorkspaceRole().getRole());
				workspaceUserInfoResponse.setLicenseProducts(getUserLicenseProductList(workspaceId, userId));
				workspaceUserInfoResponseList.add(workspaceUserInfoResponse);
			}
		}
		return new WorkspaceUserInfoListResponse(workspaceUserInfoResponseList, null);
	}

	@Transactional
	public ApiResponse<Boolean> kickOutMember(
		String workspaceId, MemberKickOutRequest memberKickOutRequest, Locale locale
	) {
		//1-1. 내보낼 유저가 일반계정인지 검증.
		UserInfoRestResponse kickedUserInfo = userRestServiceHandler.getUserRequest(
			memberKickOutRequest.getKickedUserId());
		if (kickedUserInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		if (kickedUserInfo.getUserType() == UserType.WORKSPACE_ONLY_USER
			|| kickedUserInfo.getUserType() == UserType.GUEST_USER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
		//1-2. 내보낼 유저 권한 검증
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
			workspaceId, memberKickOutRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		WorkspaceUserPermission kickedUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
			workspaceId, memberKickOutRequest.getKickedUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		//내보내는 유저가 마스터인경우
		if (kickedUserPermission.getWorkspaceRole().getRole() == Role.MASTER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
		//요청하는 유저가 마스터또는 매니저가 아닌경우
		if (!isMasterOrManagerRole(requestUserPermission.getWorkspaceRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//2. 라이선스 해제 요청
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(
			workspaceId, memberKickOutRequest.getKickedUserId());
		if (!CollectionUtils.isEmpty(myLicenseInfoListResponse.getLicenseInfoList())) {
			myLicenseInfoListResponse.getLicenseInfoList()
				.forEach(myLicenseInfoResponse -> revokeWorkspaceLicenseToUser(workspaceId,
					memberKickOutRequest.getKickedUserId(), myLicenseInfoResponse.getProductName()
				));
		}

		//3. workspace_user_permission 삭제
		workspaceUserPermissionRepository.delete(kickedUserPermission);

		//4. workspace_user 삭제
		workspaceUserRepository.delete(kickedUserPermission.getWorkspaceUser());

		//5. 결과 메일 전송
		UserInfoRestResponse masterUser = userRestServiceHandler.getUserRequest(
			requestUserPermission.getWorkspaceUser().getWorkspace().getUserId());
		Context context = mailContextHandler.getWorkspaceKickoutContext(
			requestUserPermission.getWorkspaceUser().getWorkspace().getName(), masterUser);
		applicationEventPublisher.publishEvent(
			new MailSendEvent(context, Mail.WORKSPACE_KICKOUT, locale, kickedUserInfo.getEmail()));

		//6. history 저장
		String message = messageSource.getMessage(
			"WORKSPACE_EXPELED", new String[] {masterUser.getNickname(), kickedUserInfo.getNickname()}, locale);
		applicationEventPublisher.publishEvent(new HistoryAddEvent(message, kickedUserInfo.getUuid(),
			requestUserPermission.getWorkspaceUser().getWorkspace()
		));

		return new ApiResponse<>(true);
	}

	@Profile("!onpremise")
	public abstract ApiResponse<Boolean> inviteWorkspace(
		String workspaceId, WorkspaceInviteRequest
		workspaceInviteRequest, Locale locale
	);

	@Profile("!onpremise")
	public abstract RedirectView inviteWorkspaceAccept(String sessionCode, String lang) throws IOException;

	@Profile("!onpremise")
	public abstract RedirectView inviteWorkspaceReject(String sessionCode, String lang);

	public ApiResponse<Boolean> exitWorkspace(String workspaceId, String userId, Locale locale) {
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		//마스터 유저는 워크스페이스 나가기를 할 수 없음.
		if (workspace.getUserId().equals(userId)) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		WorkspaceUserPermission workspaceUserPermission = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, userId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		//라이선스 해제
		MyLicenseInfoListResponse myLicenseInfoListResponse = getMyLicenseInfoRequestHandler(workspaceId, userId);
		if (!CollectionUtils.isEmpty(myLicenseInfoListResponse.getLicenseInfoList())) {
			myLicenseInfoListResponse.getLicenseInfoList()
				.forEach(myLicenseInfoResponse -> revokeWorkspaceLicenseToUser(workspaceId, userId,
					myLicenseInfoResponse.getProductName()
				));
		}

		workspaceUserPermissionRepository.delete(workspaceUserPermission);
		workspaceUserRepository.delete(workspaceUserPermission.getWorkspaceUser());

		//history 저장
		UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(userId);
		String message = messageSource.getMessage(
			"WORKSPACE_LEAVE", new String[] {userInfoRestResponse.getNickname()}, locale);
		applicationEventPublisher.publishEvent(new HistoryAddEvent(message, userId, workspace));
		applicationEventPublisher.publishEvent(new UserWorkspacesDeleteEvent(userId));//캐싱 삭제
		return new ApiResponse<>(true);
	}

	public WorkspaceUserInfoListResponse getSimpleWorkspaceUserList(String workspaceId) {
		List<String> workspaceUserIdList = workspaceUserRepository.getWorkspaceUserIdList(workspaceId);
		UserInfoListRestResponse userInfoListRestResponse = userRestServiceHandler.getUserListRequest(
			"", workspaceUserIdList);
		List<WorkspaceUserInfoResponse> workspaceUserInfoResponseList = userInfoListRestResponse.getUserInfoList()
			.stream()
			.map(userInfoRestResponse -> {
				WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
					userInfoRestResponse);
				WorkspaceRole role = workspaceUserPermissionRepository.findWorkspaceUserPermission(
					workspaceId, userInfoRestResponse.getUuid()).get().getWorkspaceRole();
				workspaceUserInfoResponse.setRole(role.getRole());
				workspaceUserInfoResponse.setRoleId(role.getId());
				workspaceUserInfoResponse.setLicenseProducts(
					getUserLicenseProductList(workspaceId, userInfoRestResponse.getUuid()));
				return workspaceUserInfoResponse;
			})
			.collect(Collectors.toList());

		return new WorkspaceUserInfoListResponse(workspaceUserInfoResponseList, null);
	}

	/**
	 * 워크스페이스 소속 멤버 플랜 리스트 조회
	 *
	 * @param workspaceId - 조회 대상 워크스페이스 식별자
	 * @param pageable    -  페이징
	 * @return - 멤버 플랜 리스트
	 */
	public WorkspaceUserLicenseListResponse getLicenseWorkspaceUserList(
		String workspaceId, com.virnect.workspace.global.common.PageRequest pageRequest
	) {
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null
			|| workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
		}

		List<WorkspaceUserLicenseInfoResponse> workspaceUserLicenseInfoList = new ArrayList<>();

		workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
			.forEach(licenseProductInfoResponse -> {
				licenseProductInfoResponse.getLicenseInfoList().stream()
					.filter(licenseInfoResponse -> licenseInfoResponse.getStatus().equals(LicenseStatus.USE))
					.forEach(licenseInfoResponse -> {
						UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(
							licenseInfoResponse.getUserId());
						WorkspaceUserLicenseInfoResponse workspaceUserLicenseInfo = restMapStruct.userInfoRestResponseToWorkspaceUserLicenseInfoResponse(
							userInfoRestResponse);
						workspaceUserLicenseInfo.setLicenseType(licenseProductInfoResponse.getLicenseType());
						workspaceUserLicenseInfo.setProductName(licenseProductInfoResponse.getProductName());
						workspaceUserLicenseInfoList.add(workspaceUserLicenseInfo);
					});
			});
		if (workspaceUserLicenseInfoList.isEmpty()) {
			PageMetadataRestResponse pageMetadataRestResponse = new PageMetadataRestResponse();
			pageMetadataRestResponse.setCurrentPage(pageRequest.of().getPageNumber());
			pageMetadataRestResponse.setCurrentSize(pageRequest.of().getPageSize());
			pageMetadataRestResponse.setTotalElements(0);
			pageMetadataRestResponse.setTotalPage(0);
			return new WorkspaceUserLicenseListResponse(workspaceUserLicenseInfoList, new PageMetadataRestResponse());
		}

		List<WorkspaceUserLicenseInfoResponse> sortedWorkspaceUserLicenseList
			= workspaceUserLicenseInfoList.stream()
			.sorted(Comparator.comparing(
				getWorkspaceUserLicenseInfoResponseSortKey(pageRequest.getSortName()),
				getSortDirection(pageRequest.getSortDirection())
			))
			.collect(Collectors.toList());

		CustomPageResponse<WorkspaceUserLicenseInfoResponse> customPageResponse
			= new CustomPageHandler<WorkspaceUserLicenseInfoResponse>().paging(
			pageRequest.of().getPageNumber(), pageRequest.of().getPageSize(), sortedWorkspaceUserLicenseList);
		return new WorkspaceUserLicenseListResponse(
			customPageResponse.getAfterPagingList(), customPageResponse.getPageMetadataResponse());
	}

	private Function<WorkspaceUserLicenseInfoResponse, String> getWorkspaceUserLicenseInfoResponseSortKey
		(
			String
				sortName
		) {
		if (sortName.equalsIgnoreCase("plan")) {
			return WorkspaceUserLicenseInfoResponse::getProductName;
		}
		if (sortName.equalsIgnoreCase("nickName")) {
			return WorkspaceUserLicenseInfoResponse::getNickName;
		}
		return WorkspaceUserLicenseInfoResponse::getProductName;
	}

	private Comparator<String> getSortDirection(String sortDirection) {
		if (sortDirection.equalsIgnoreCase("asc")) {
			return Comparator.nullsFirst(Comparator.naturalOrder());
		}
		if (sortDirection.equalsIgnoreCase("desc")) {
			return Comparator.nullsFirst(Comparator.reverseOrder());
		}
		return Comparator.nullsFirst(Comparator.naturalOrder());
	}

	@Transactional
	public WorkspaceMemberInfoListResponse createWorkspaceMemberAccount(
		String workspaceId, MemberAccountCreateRequest memberAccountCreateRequest
	) {
		//1-1. 계정 유형 체크
		if (memberAccountCreateRequest.existMasterRoleUser() || memberAccountCreateRequest.existGuestRoleUser()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_CREATE_FAIL);
		}

		//1-2. 요청한 사람의 권한 체크
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findWorkspaceUserPermission(
			workspaceId, memberAccountCreateRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		if (!isMasterOrManagerRole(requestUserPermission.getWorkspaceRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//1-3. 워크스페이스 멤버 제한 수 체크
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = getWorkspaceLicensesByWorkspaceId(
			workspaceId);
		checkWorkspaceMaxUserAmount(
			workspaceId, memberAccountCreateRequest.getMemberAccountCreateRequest().size(),
			workspaceLicensePlanInfoResponse
		);

		List<String> responseLicense = new ArrayList<>();
		List<WorkspaceUserInfoResponse> workspaceUserInfoResponseList = new ArrayList<>();

		for (MemberAccountCreateInfo memberAccountCreateInfo : memberAccountCreateRequest.getMemberAccountCreateRequest()) {
			//1. user-server 멤버 정보 등록 api 요청
			MemberRegistrationRequest memberRegistrationRequest = new MemberRegistrationRequest();
			memberRegistrationRequest.setEmail(memberAccountCreateInfo.getId());
			memberRegistrationRequest.setPassword(memberAccountCreateInfo.getPassword());
			memberRegistrationRequest.setMasterUUID(
				requestUserPermission.getWorkspaceUser().getWorkspace().getUserId());

			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.registerUserRequest(
				memberRegistrationRequest);
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_CREATE_FAIL);
			}

			//2. license-server grant api 요청
			if (memberAccountCreateInfo.isPlanRemote()) {
				MyLicenseInfoResponse myLicenseInfoResponse = grantWorkspaceLicenseToUser(
					workspaceId, userInfoRestResponse.getUuid(), "REMOTE");
				responseLicense.add(myLicenseInfoResponse.getProductName());
			}
			if (memberAccountCreateInfo.isPlanMake()) {
				MyLicenseInfoResponse myLicenseInfoResponse = grantWorkspaceLicenseToUser(
					workspaceId, userInfoRestResponse.getUuid(), "MAKE");
				responseLicense.add(myLicenseInfoResponse.getProductName());
			}
			if (memberAccountCreateInfo.isPlanView()) {
				MyLicenseInfoResponse myLicenseInfoResponse = grantWorkspaceLicenseToUser(
					workspaceId, userInfoRestResponse.getUuid(), "VIEW");
				responseLicense.add(myLicenseInfoResponse.getProductName());
			}

			//3. workspace 권한 및 소속 부여
			WorkspaceUser newWorkspaceUser = WorkspaceUser.builder()
				.userId(userInfoRestResponse.getUuid())
				.workspace(requestUserPermission.getWorkspaceUser().getWorkspace())
				.build();
			WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(
				Role.valueOf(memberAccountCreateInfo.getRole()))
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_ROLE_NOT_FOUND));
			WorkspacePermission workspacePermission = workspacePermissionRepository.findByPermission(Permission.ALL)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_PERMISSION_NOT_FOUND));
			WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
				.workspaceUser(newWorkspaceUser)
				.workspacePermission(workspacePermission)
				.workspaceRole(workspaceRole)
				.build();

			workspaceUserRepository.save(newWorkspaceUser);
			workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

			log.info(
				"[CREATE WORKSPACE MEMBER ACCOUNT] Workspace add user success. Request User UUID : [{}], Role : [{}], JoinDate : [{}]",
				userInfoRestResponse.getUuid(), workspaceRole.getRole(), newWorkspaceUser.getCreatedDate()
			);

			//4. response
			WorkspaceUserInfoResponse memberInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
				userInfoRestResponse);
			memberInfoResponse.setRole(newWorkspaceUserPermission.getWorkspaceRole().getRole());
			memberInfoResponse.setRoleId(newWorkspaceUserPermission.getWorkspaceRole().getId());
			memberInfoResponse.setJoinDate(newWorkspaceUser.getCreatedDate());
			memberInfoResponse.setLicenseProducts(responseLicense.toArray(new String[0]));
			workspaceUserInfoResponseList.add(memberInfoResponse);
		}

		return new WorkspaceMemberInfoListResponse(workspaceUserInfoResponseList);
	}

	@Transactional
	public abstract boolean deleteWorkspaceMemberAccount(
		String workspaceId, MemberAccountDeleteRequest
		memberAccountDeleteRequest
	);

	@Transactional
	public WorkspaceMemberPasswordChangeResponse memberPasswordChange(
		WorkspaceMemberPasswordChangeRequest passwordChangeRequest, String workspaceId
	) {
		//1. 대상유저가 전용 계정인지 체크
		UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.getUserRequest(
			passwordChangeRequest.getUserId());
		if (userInfoRestResponse.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		if (userInfoRestResponse.getUserType() != UserType.WORKSPACE_ONLY_USER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_PASSWORD_CHANGE);
		}

		//2. 요청 권한 체크
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, passwordChangeRequest.getRequestUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUserPermission updateUserPermission = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, passwordChangeRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		if (!passwordChangeRequest.getRequestUserId().equals(passwordChangeRequest.getUserId())) {
			// 요청한 사람이 마스터 또는 매니저여야 한다.
			if (!isMasterOrManagerRole(requestUserPermission.getWorkspaceRole())){
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
			// 매니저 유저는 매니저 유저를 수정 할 수 없다.
			if (isBothManagerRole(requestUserPermission.getWorkspaceRole(),updateUserPermission.getWorkspaceRole())) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
		}

		MemberUserPasswordChangeResponse responseMessage = userRestServiceHandler.userPasswordChangeRequest(
			new MemberUserPasswordChangeRequest(
				passwordChangeRequest.getUserId(),
				passwordChangeRequest.getPassword()
			));
		if (responseMessage.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_PASSWORD_CHANGE);
		}

		return new WorkspaceMemberPasswordChangeResponse(
			passwordChangeRequest.getRequestUserId(),
			passwordChangeRequest.getUserId(),
			responseMessage.getPasswordChangedDate()
		);
	}

	/**
	 * 워크스페이스 게스트 계정 생성
	 *
	 * @param workspaceId              - 요청 워크스페이스 식별자
	 * @param memberGuestCreateRequest - 게스트 계정 생성 요청 정보
	 * @return - 생성된 게스트 계정 목록
	 */
	@Transactional
	public WorkspaceMemberInfoListResponse createWorkspaceMemberGuest(
		String
			workspaceId, MemberGuestCreateRequest
		memberGuestCreateRequest
	) {
		//1-1. 요청한 사람의 권한 체크
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		checkGuestMemberManagementPermission(workspace, memberGuestCreateRequest.getUserId());

		//1-2. 라이선스 갯수 유효성 체크
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = getWorkspaceLicensesByWorkspaceId(
			workspaceId);
		checkGuestMemberCreateLicense(memberGuestCreateRequest, workspaceLicensePlanInfoResponse);

		//1-3. 워크스페이스 멤버 제한 수 체크
		int requestGuestUserAmount =
			memberGuestCreateRequest.getPlanRemoteAndView() + memberGuestCreateRequest.getPlanRemote()
				+ memberGuestCreateRequest.getPlanView();
		checkWorkspaceMaxUserAmount(workspaceId, requestGuestUserAmount, workspaceLicensePlanInfoResponse);

		//3. 게스트 계정 생성
		List<WorkspaceUserInfoResponse> workspaceMemberInfoList = new ArrayList<>();
		GuestMemberRegistrationRequest guestMemberRegistrationRequest = new GuestMemberRegistrationRequest();
		guestMemberRegistrationRequest.setMasterUserUUID(workspace.getUserId());
		guestMemberRegistrationRequest.setWorkspaceUUID(workspaceId);
		for (int i = 0; i < memberGuestCreateRequest.getPlanRemoteAndView(); i++) {
			//게정 생성
			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.guestUserRegistrationRequest(
				guestMemberRegistrationRequest);
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_CREATE);
			}
			//라이선스 할당
			grantWorkspaceLicenseToUser(workspaceId, userInfoRestResponse.getUuid(), "REMOTE");
			grantWorkspaceLicenseToUser(workspaceId, userInfoRestResponse.getUuid(), "VIEW");
			//게스트 멤버 저장
			WorkspaceUser workspaceUser = WorkspaceUser.builder()
				.userId(userInfoRestResponse.getUuid())
				.workspace(workspace)
				.build();
			WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(Role.GUEST)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_ROLE_NOT_FOUND));
			WorkspacePermission workspacePermission = workspacePermissionRepository.findByPermission(Permission.ALL)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_PERMISSION_NOT_FOUND));
			WorkspaceUserPermission workspaceUserPermission = WorkspaceUserPermission.builder()
				.workspaceUser(workspaceUser)
				.workspaceRole(workspaceRole)
				.workspacePermission(workspacePermission)
				.build();
			workspaceUserRepository.save(workspaceUser);
			workspaceUserPermissionRepository.save(workspaceUserPermission);
			//응답
			WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
				userInfoRestResponse);
			workspaceUserInfoResponse.setRole(workspaceRole.getRole());
			workspaceUserInfoResponse.setRoleId(workspaceRole.getId());
			workspaceUserInfoResponse.setJoinDate(workspaceUser.getCreatedDate());
			workspaceUserInfoResponse.setLicenseProducts(new String[] {"REMOTE", "VIEW"});
			workspaceMemberInfoList.add(workspaceUserInfoResponse);
		}
		for (int i = 0; i < memberGuestCreateRequest.getPlanRemote(); i++) {
			//게정 생성
			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.guestUserRegistrationRequest(
				guestMemberRegistrationRequest);
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_CREATE);
			}
			//라이선스 할당
			grantWorkspaceLicenseToUser(workspaceId, userInfoRestResponse.getUuid(), "REMOTE");
			//게스트 멤버 저장
			WorkspaceUser workspaceUser = WorkspaceUser.builder()
				.userId(userInfoRestResponse.getUuid())
				.workspace(workspace)
				.build();
			WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(Role.GUEST)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_ROLE_NOT_FOUND));
			WorkspacePermission workspacePermission = workspacePermissionRepository.findByPermission(Permission.ALL)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_PERMISSION_NOT_FOUND));
			WorkspaceUserPermission workspaceUserPermission = WorkspaceUserPermission.builder()
				.workspaceUser(workspaceUser)
				.workspaceRole(workspaceRole)
				.workspacePermission(workspacePermission)
				.build();
			workspaceUserRepository.save(workspaceUser);
			workspaceUserPermissionRepository.save(workspaceUserPermission);
			//응답
			WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
				userInfoRestResponse);
			workspaceUserInfoResponse.setRole(workspaceRole.getRole());
			workspaceUserInfoResponse.setRoleId(workspaceRole.getId());
			workspaceUserInfoResponse.setJoinDate(workspaceUser.getCreatedDate());
			workspaceUserInfoResponse.setLicenseProducts(new String[] {"REMOTE"});
			workspaceMemberInfoList.add(workspaceUserInfoResponse);
		}
		for (int i = 0; i < memberGuestCreateRequest.getPlanView(); i++) {
			//게정 생성
			UserInfoRestResponse userInfoRestResponse = userRestServiceHandler.guestUserRegistrationRequest(
				guestMemberRegistrationRequest);
			if (userInfoRestResponse.isEmtpy()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_CREATE);
			}
			//라이선스 할당
			grantWorkspaceLicenseToUser(workspaceId, userInfoRestResponse.getUuid(), "VIEW");
			//게스트 멤버 저장
			WorkspaceUser workspaceUser = WorkspaceUser.builder()
				.userId(userInfoRestResponse.getUuid())
				.workspace(workspace)
				.build();
			WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(Role.GUEST)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_ROLE_NOT_FOUND));
			WorkspacePermission workspacePermission = workspacePermissionRepository.findByPermission(Permission.ALL)
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_PERMISSION_NOT_FOUND));
			WorkspaceUserPermission workspaceUserPermission = WorkspaceUserPermission.builder()
				.workspaceUser(workspaceUser)
				.workspaceRole(workspaceRole)
				.workspacePermission(workspacePermission)
				.build();
			workspaceUserRepository.save(workspaceUser);
			workspaceUserPermissionRepository.save(workspaceUserPermission);
			//응답
			WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
				userInfoRestResponse);
			workspaceUserInfoResponse.setRole(workspaceRole.getRole());
			workspaceUserInfoResponse.setRoleId(workspaceRole.getId());
			workspaceUserInfoResponse.setJoinDate(workspaceUser.getCreatedDate());
			workspaceUserInfoResponse.setLicenseProducts(new String[] {"VIEW"});

			workspaceMemberInfoList.add(workspaceUserInfoResponse);
		}
		return new WorkspaceMemberInfoListResponse(workspaceMemberInfoList);
	}

	/**
	 * 게스트 계정 생성 시 라이선스 갯수 검증
	 *
	 * @param memberGuestCreateRequest         - 게스트 계정 생성 요청 정보
	 * @param workspaceLicensePlanInfoResponse - 워크스페이스 라이선스 정보
	 */
	private void checkGuestMemberCreateLicense(
		MemberGuestCreateRequest
			memberGuestCreateRequest, WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse
	) {
		Optional<Integer> optionalRemote = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
			.stream()
			.filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals("REMOTE"))
			.map(WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse::getUnUseLicenseAmount)
			.findFirst();
		Optional<Integer> optionalView = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
			.stream()
			.filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals("VIEW"))
			.map(WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse::getUnUseLicenseAmount)
			.findFirst();
		if (memberGuestCreateRequest.getPlanRemoteAndView() > 0) {
			if (!optionalRemote.isPresent() || !optionalView.isPresent()
				|| memberGuestCreateRequest.getPlanRemoteAndView() > optionalRemote.get()
				|| memberGuestCreateRequest.getPlanRemoteAndView() > optionalView.get()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_CREATE_LACK_LICENSE);
			}
		}
		if (memberGuestCreateRequest.getPlanRemote() > 0) {
			if (!optionalRemote.isPresent() || memberGuestCreateRequest.getPlanRemote() > optionalRemote.get()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_CREATE_LACK_LICENSE);
			}
		}
		if (memberGuestCreateRequest.getPlanView() > 0) {
			if (!optionalView.isPresent() || memberGuestCreateRequest.getPlanView() > optionalView.get()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_GUEST_USER_CREATE_LACK_LICENSE);
			}
		}
	}

	/**
	 * 게스트 계정 생성, 삭제, 정보편집에 대한 권한 검증
	 *
	 * @param workspace     - 검증 대상 워크스페이스 정보
	 * @param requestUserId - 계정 생성 요청 유저 식별자
	 */
	public void checkGuestMemberManagementPermission(Workspace workspace, String requestUserId) {
		WorkspaceUserPermission requestUserPermission = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, requestUserId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		Role requestUserRole = requestUserPermission.getWorkspaceRole().getRole();
		if (requestUserRole != Role.MASTER && requestUserRole != Role.MANAGER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
	}

	/**
	 * 라이선스서버 - 워크스페이스 서버 라이선스 조회
	 *
	 * @param workspaceId - 요청 워크스페이스 식별자
	 * @return - 워크스페이스 라이선스 정보
	 */
	WorkspaceLicensePlanInfoResponse getWorkspaceLicensesByWorkspaceId(String workspaceId) {
		ApiResponse<WorkspaceLicensePlanInfoResponse> apiResponse = licenseRestService.getWorkspaceLicenses(
			workspaceId);
		if (apiResponse.getCode() != 200) {
			log.error(
				"[GET WORKSPACE LICENSE PLAN INFO BY WORKSPACE UUID] response message : {}", apiResponse.getMessage());
			return new WorkspaceLicensePlanInfoResponse();
		}
		return apiResponse.getData();
	}

	/**
	 * 워크스페이스 최대 멤버 제한 수 검증
	 *
	 * @param workspaceId                      - 검증 대상 워크스페이스 식별자
	 * @param requestUserAmount                - 참여 요청 유저 수
	 * @param workspaceLicensePlanInfoResponse - 워크스페이스 라이선스 정보
	 */
	void checkWorkspaceMaxUserAmount(
		String workspaceId, int requestUserAmount, WorkspaceLicensePlanInfoResponse
		workspaceLicensePlanInfoResponse
	) {
		//마스터포함 워크스페이스 전체 유저 수 조회
		long workspaceUserAmount = workspaceUserRepository.countByWorkspace_Uuid(workspaceId);
		log.info(
			"[CHECK WORKSPACE MAX USER AMOUNT] current user amount : {}, request user amount : {}, max user amount by license : {}",
			workspaceUserAmount, requestUserAmount, workspaceLicensePlanInfoResponse.getMaxUserAmount()
		);
		if (workspaceLicensePlanInfoResponse.getMaxUserAmount() > 0) {
			// 라이선스를 구매한 워크스페이스는 라이선스에 종속된 값으로 체크
			if (requestUserAmount + workspaceUserAmount > workspaceLicensePlanInfoResponse.getMaxUserAmount()) {
				log.error(
					"[CHECK WORKSPACE MAX USER AMOUNT] maximum workspace user amount(by license) : [{}], request user amount [{}], current workspace user amount : [{}]",
					workspaceLicensePlanInfoResponse.getMaxUserAmount(), requestUserAmount, workspaceUserAmount
				);
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_MAX_USER_AMOUNT_OVER);
			}
		} else {
			// 라이선스를 구매하지 않은 워크스페이스는 기본값으로 체크
			if (requestUserAmount + workspaceUserAmount > MAX_WORKSPACE_USER_AMOUNT) {
				log.error(
					"[CHECK WORKSPACE MAX USER AMOUNT] maximum workspace user amount(by workspace) : [{}], request user amount [{}], current workspace user amount : [{}]",
					MAX_WORKSPACE_USER_AMOUNT, requestUserAmount, workspaceUserAmount
				);
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_MAX_USER_AMOUNT_OVER);
			}
		}
	}

	/**
	 * 워크스페이스 게스트 계정 삭제 및 워크스페이스에서 내보내기
	 *
	 * @param workspaceId             - 삭제 대상 워크스페이스 식별자
	 * @param memberGuestDeleteRequest - 삭제 대상 유저 및 삭제 요청 유저 정보
	 * @return - 삭제 결과
	 */
	public abstract MemberSeatDeleteResponse deleteWorkspaceMemberSeat(
		String workspaceId, MemberGuestDeleteRequest
		memberGuestDeleteRequest
	);

	/**
	 * 전용계정, 게스트 계정 프로필 이미지 수정
	 * 본인에 대한 프로필 이미지 변경은 권한체크하지 않음.
	 * 본인이 아닌경우에는 마스터, 매니저만 프로필 이미지를 변경 할 수 있음.
	 * 단 매니저는 다른 매니저의 프로필을 변경할수 없음.
	 * @param workspaceId - 워크스페이스 식별자
	 * @param memberProfileUpdateRequest - 프로필 이미지 변경 요청 정보
	 * @return - 프로필 이미지 업데이트 정보
	 */
	public MemberProfileUpdateResponse updateWorkspaceOnlyUserOrGuestUserProfile(
		String workspaceId, MemberProfileUpdateRequest memberProfileUpdateRequest
	) {
		//1-1. 변경 대상 유저 타입 검증
		UserInfoRestResponse userInfo = userRestServiceHandler.getUserRequest(memberProfileUpdateRequest.getUserId());
		if (userInfo.isEmtpy()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND);
		}
		if (userInfo.getUserType() == UserType.USER) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE);
		}

		//1-2. 변경 요청 유저 권한 검증
		if (!memberProfileUpdateRequest.isUserSelfUpdateRequest()) {
			WorkspaceRole requestUserRole = workspaceRoleRepository.findWorkspaceRole(
				workspaceId, memberProfileUpdateRequest.getRequestUserId())
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
			WorkspaceRole updateUserRole = workspaceRoleRepository.findWorkspaceRole(
				workspaceId, memberProfileUpdateRequest.getUserId())
				.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
			if (!isMasterOrManagerRole(requestUserRole)) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
			if (isBothManagerRole(requestUserRole, updateUserRole)) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
		}

		//2. 프로필 이미지 변경 요청
		if (memberProfileUpdateRequest.getUpdateAsDefaultImage() != null
			&& memberProfileUpdateRequest.getUpdateAsDefaultImage()) {
			UserProfileUpdateResponse userProfileUpdateResponse = userRestServiceHandler.modifyUserDefaultProfileRequest(
				memberProfileUpdateRequest.getUserId());
			if (userProfileUpdateResponse.isEmpty()) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE);
			}
			return new MemberProfileUpdateResponse(
				true, userProfileUpdateResponse.getProfile(), memberProfileUpdateRequest.getUserId(),
				LocalDateTime.now()
			);

		}
		UserProfileUpdateResponse userProfileUpdateResponse = userRestServiceHandler.modifyUserProfileRequest(
			memberProfileUpdateRequest.getUserId(), memberProfileUpdateRequest.getProfile());
		if (userProfileUpdateResponse.isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_INFO_UPDATE);
		}

		//3. 응답
		return new MemberProfileUpdateResponse(
			true, userProfileUpdateResponse.getProfile(), memberProfileUpdateRequest.getUserId(), LocalDateTime.now());
	}

	public boolean isBothManagerRole(WorkspaceRole workspaceRole1, WorkspaceRole workspaceRole2) {
		return workspaceRole1.getRole() == Role.MANAGER && workspaceRole2.getRole() == Role.MANAGER;
	}

	public boolean isMasterOrManagerRole(WorkspaceRole workspaceRole) {
		return workspaceRole.getRole() == Role.MASTER || workspaceRole.getRole() == Role.MANAGER;
	}

}
