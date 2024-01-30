package com.virnect.platform.workspace.application.workspaceuser;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.platform.feign.rest.account.AccountRestServiceHandler;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoListRestResponse;
import com.virnect.platform.feign.rest.account.dto.response.UserInfoRestResponse;
import com.virnect.platform.license.application.LicenseService;
import com.virnect.platform.license.dto.license.response.UserLicenseInfo;
import com.virnect.platform.workspace.dao.workspaceuser.WorkspaceUserRepository;
import com.virnect.platform.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.platform.workspace.domain.workspace.Role;
import com.virnect.platform.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.platform.workspace.dto.response.PageMetadataRestResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceUserInfoListResponse;
import com.virnect.platform.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.platform.workspace.global.util.CollectionJoinUtil;

/**
 * Project        : PF-Service
 * DATE           : 2024-01-30
 * AUTHOR         : jtkim (Jintae kim)
 * EMAIL          : jtkim@virnect.com
 * DESCRIPTION    :
 * ===========================================================
 * DATE            AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-01-30      jtkim          최초 생성
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkspaceUserServiceV2 {

	private static final String ROLE_FILTER = ".*(?i)MASTER.*|.*(?i)MANAGER.*|.*(?i)MEMBER.*|.*(?i)GUEST.*";
	private static final String LICENSE_FILTER = ".*(?i)REMOTE.*|.*(?i)MAKE.*|.*(?i)VIEW.*";
	private final WorkspaceUserRepository workspaceUserRepository;
	private final AccountRestServiceHandler accountRestServiceHandler;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final LicenseService licenseService;

	public WorkspaceUserInfoListResponse getMembers(
		String workspaceId, String search, String filter, List<Role> roleFilter, String userTypeFilter,
		String planFilter, com.virnect.platform.workspace.global.common.PageRequest pageRequest, boolean paging
	) {
		//1. 정렬 검증으로 Pageable 재정의
		Pageable newPageable = pageRequest.of();

		//워크스페이스 소속 전체 유저
		List<String> resultUserIdList = workspaceUserRepository.getWorkspaceUserIdList(workspaceId);

		if (StringUtils.hasText(search)) {
			UserInfoListRestResponse userInfoListRestResponse = accountRestServiceHandler.getUserListRequest(
				search, resultUserIdList);
			resultUserIdList = userInfoListRestResponse.getUserInfoList()
				.stream()
				.map(UserInfoRestResponse::getUuid)
				.collect(Collectors.toList());
		}

		if (StringUtils.hasText(filter)) {
			//3-1. 라이선스 플랜으로 필터링
			if (filter.matches(LICENSE_FILTER) && !resultUserIdList.isEmpty()) {
				resultUserIdList = filterUserIdListByPlan(workspaceId, resultUserIdList, filter);
			}
			//3-2. 워크스페이스 역할로 필터링
			else if (filter.matches(ROLE_FILTER) && !resultUserIdList.isEmpty()) {

				List<Role> roleList = Role.getMatchedList(filter);
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
			UserInfoListRestResponse userInfoListRestResponse = accountRestServiceHandler.getUserListRequest(
				"", resultUserIdList);

			String[] userTypes =
				userTypeFilter.toUpperCase().split(",").length == 0 ? new String[] {userTypeFilter.toUpperCase()} :
					userTypeFilter.toUpperCase().split(",");

			resultUserIdList = userInfoListRestResponse.getUserInfoList()
				.stream()
				.filter(userInfoRestResponse -> Arrays.asList(userTypes)
					.contains(userInfoRestResponse.getUserType().name()))
				.map(UserInfoRestResponse::getUuid)
				.collect(Collectors.toList());

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
				workspaceId, workspaceUserPermissionList
			);
			return new WorkspaceUserInfoListResponse(workspaceUserListResponse);
		}

		//5-2. 최종적으로 필터링된 유저들을 페이징한다.
		Page<WorkspaceUserPermission> workspaceUserPermissionPage = workspaceUserPermissionRepository.getWorkspaceUserPageByInUserList(
			resultUserIdList, newPageable, workspaceId);
		List<WorkspaceUserInfoResponse> workspaceUserListResponse = generateWorkspaceUserListResponse(
			workspaceId, workspaceUserPermissionPage.toList());

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(workspaceUserPermissionPage.getTotalElements());
		pageMetadataResponse.setTotalPage(workspaceUserPermissionPage.getTotalPages());
		pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
		pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

		if (pageRequest.getSortName().equalsIgnoreCase("email") || pageRequest.getSortName()
			.equalsIgnoreCase("nickname")) {
			return new WorkspaceUserInfoListResponse(
				getSortedMemberList(pageRequest, workspaceUserListResponse), pageMetadataResponse);
		}
		return new WorkspaceUserInfoListResponse(workspaceUserListResponse, pageMetadataResponse);
	}

	public List<String> filterUserIdListByPlan(String workspaceId, List<String> userIdList, String planFilter) {

		List<UserLicenseInfo> userLicenseInfoList = licenseService.getUserLicenseInfos(workspaceId, userIdList, planFilter)
			.getUserLicenseInfos();

		return userLicenseInfoList.stream()
			.filter(
				userLicenseInfo -> userLicenseInfo.getProductName().toUpperCase().contains(planFilter.toUpperCase()))
			.map(com.virnect.platform.license.dto.license.response.UserLicenseInfo::getUserId)
			.collect(Collectors.toList());
	}

	public List<WorkspaceUserInfoResponse> generateWorkspaceUserListResponse(
		String workspaceId, List<WorkspaceUserPermission> workspaceUserPermissionList
	) {
		List<String> workspaceUserIdList = workspaceUserPermissionList.stream()
			.map(workspaceUserPermission -> workspaceUserPermission.getWorkspaceUser().getUserId())
			.collect(Collectors.toList());

		//license
		List<UserLicenseInfo> userLicenseInfoList = licenseService.getUserLicenseInfos(workspaceId, workspaceUserIdList, "").getUserLicenseInfos();

		Map<String, List<String>> productLicenseArray = UserLicenseInfo.getProductLicenseGroupedByUser(userLicenseInfoList);

		//user
		List<UserInfoRestResponse> userInfoList = accountRestServiceHandler.getUserListRequest("", workspaceUserIdList).getUserInfoList();

		List<WorkspaceUserInfoResponse> workspaceUserInfoResponseList = CollectionJoinUtil.collections(
				workspaceUserPermissionList, userInfoList)
			.when((workspaceUserPermission, userInfoRestResponse) ->
				workspaceUserPermission.getWorkspaceUser().getUserId().equals(userInfoRestResponse.getUuid()))
			.then(
				WorkspaceUserInfoResponse::createWorkspaceUserInfoResponse);

		//라이센스 할당.
		workspaceUserInfoResponseList
			.stream()
			.filter(workspaceUserInfoResponse -> productLicenseArray.containsKey(workspaceUserInfoResponse.getUuid()))
			.forEach(
				workspaceUserInfoResponse -> workspaceUserInfoResponse.setUserLicenseProductForWorkspaceUserInfoResponse(
					productLicenseArray.get(workspaceUserInfoResponse.getUuid()).toArray(new String[0]))
			);

		return workspaceUserInfoResponseList;
	}

	public List<WorkspaceUserInfoResponse> getSortedMemberList(
		com.virnect.platform.workspace.global.common.PageRequest
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
}
