package com.virnect.workspace.application.workspace;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.application.license.LicenseRestService;
import com.virnect.workspace.application.license.dto.WorkspaceLicensePlanInfoResponse;
import com.virnect.workspace.application.user.UserRestService;
import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.dao.history.HistoryRepository;
import com.virnect.workspace.dao.setting.WorkspaceSettingRepository;
import com.virnect.workspace.dao.workspace.WorkspaceRepository;
import com.virnect.workspace.dao.workspaceuser.WorkspaceUserRepository;
import com.virnect.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.workspace.dto.request.WorkspaceCreateRequest;
import com.virnect.workspace.dto.request.WorkspaceFaviconUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceLogoUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceRemoteLogoUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceTitleUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceUpdateRequest;
import com.virnect.workspace.dto.response.PageMetadataRestResponse;
import com.virnect.workspace.dto.response.WorkspaceCustomSettingResponse;
import com.virnect.workspace.dto.response.WorkspaceFaviconUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoDTO;
import com.virnect.workspace.dto.response.WorkspaceInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceLicenseInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceLogoUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceRemoteLogoUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceSecessionResponse;
import com.virnect.workspace.dto.response.WorkspaceTitleUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceUserInfoResponse;
import com.virnect.workspace.event.message.MailContextHandler;
import com.virnect.workspace.event.message.MailSendEvent;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.mapper.rest.RestMapStruct;
import com.virnect.workspace.global.common.mapper.workspace.WorkspaceMapStruct;
import com.virnect.workspace.global.constant.LicenseProduct;
import com.virnect.workspace.global.constant.Mail;
import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.infra.file.DefaultImageName;
import com.virnect.workspace.infra.file.FileService;

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
public abstract class WorkspaceService {
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceUserRepository workspaceUserRepository;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final UserRestService userRestService;
	private final FileService fileUploadService;
	private final HistoryRepository historyRepository;
	private final LicenseRestService licenseRestService;
	private final WorkspaceMapStruct workspaceMapStruct;
	private final RestMapStruct restMapStruct;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final MailContextHandler mailContextHandler;
	private final WorkspaceSettingRepository workspaceSettingRepository;

	/**
	 * 워크스페이스 생성
	 *
	 * @param workspaceCreateRequest - 생성 할 워크스페이스 정보
	 * @return - 생성 된 워크스페이스 정보
	 */
	@Transactional
	public abstract WorkspaceInfoDTO createWorkspace(WorkspaceCreateRequest workspaceCreateRequest);

	/**
	 * 사용자 소속 워크스페이스 조회
	 *
	 * @param userId - 사용자 uuid
	 * @return - 소속된 워크스페이스 정보
	 */
	public WorkspaceInfoListResponse getUserWorkspaces(
		String userId, com.virnect.workspace.global.common.PageRequest pageRequest
	) {
		Page<WorkspaceUserPermission> workspaceUserPermissionPage = workspaceUserPermissionRepository.findByWorkspaceUser_UserId(
			userId, pageRequest.of());
		List<WorkspaceInfoListResponse.WorkspaceInfo> workspaceList = new ArrayList<>();

		for (WorkspaceUserPermission workspaceUserPermission : workspaceUserPermissionPage) {
			WorkspaceUser workspaceUser = workspaceUserPermission.getWorkspaceUser();
			Workspace workspace = workspaceUser.getWorkspace();

			WorkspaceInfoListResponse.WorkspaceInfo workspaceInfo = workspaceMapStruct.workspaceToWorkspaceInfo(
				workspace);
			workspaceInfo.setJoinDate(workspaceUser.getCreatedDate());

			UserInfoRestResponse userInfoRestResponse = userRestService.getUserInfoByUserId(workspace.getUserId())
				.getData();
			workspaceInfo.setMasterName(userInfoRestResponse.getName());
			workspaceInfo.setMasterProfile(userInfoRestResponse.getProfile());
			workspaceInfo.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
			workspaceInfo.setMasterNickName(userInfoRestResponse.getNickname());
			workspaceInfo.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());
			workspaceList.add(workspaceInfo);
		}

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(workspaceUserPermissionPage.getTotalElements());
		pageMetadataResponse.setTotalPage(workspaceUserPermissionPage.getTotalPages());
		pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber());
		pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

		return new WorkspaceInfoListResponse(workspaceList, pageMetadataResponse);
	}

	/**
	 * 워크스페이스 정보 조회
	 *
	 * @param workspaceId - 워크스페이스 uuid
	 * @return - 워크스페이스 정보
	 */
	public WorkspaceInfoResponse getWorkspaceDetailInfo(String workspaceId) {
		//workspace 정보 set
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceInfoDTO workspaceInfo = workspaceMapStruct.workspaceToWorkspaceInfoDTO(workspace);
		workspaceInfo.setMasterUserId(workspace.getUserId());

		//user 정보 set
		List<WorkspaceUserPermission> workspaceUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_Workspace(
			workspace);
		List<WorkspaceUserInfoResponse> userInfoList = workspaceUserPermissionList.stream()
			.map(workspaceUserPermission -> {
				UserInfoRestResponse userInfoRestResponse = userRestService.getUserInfoByUserId(
					workspaceUserPermission.getWorkspaceUser().getUserId()).getData();
				WorkspaceUserInfoResponse workspaceUserInfoResponse = restMapStruct.userInfoRestResponseToWorkspaceUserInfoResponse(
					userInfoRestResponse);
				workspaceUserInfoResponse.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
				return workspaceUserInfoResponse;
			})
			.collect(Collectors.toList());

		//role 정보 set
		long masterUserCount = workspaceUserPermissionList.stream()
			.filter(workspaceUserPermission -> workspaceUserPermission.getWorkspaceRole().getRole() == Role.MASTER)
			.count();
		long managerUserCount = workspaceUserPermissionList.stream()
			.filter(workspaceUserPermission -> workspaceUserPermission.getWorkspaceRole().getRole() == (Role.MANAGER))
			.count();
		long memberUserCount = workspaceUserPermissionList.stream()
			.filter(workspaceUserPermission -> workspaceUserPermission.getWorkspaceRole().getRole() == (Role.MEMBER))
			.count();

		//plan 정보 set
		int remotePlanCount = 0, makePlanCount = 0, viewPlanCount = 0;
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() != null
			&& !workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			for (WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse licenseProductInfoResponse : workspaceLicensePlanInfoResponse.getLicenseProductInfoList()) {
				if (licenseProductInfoResponse.getProductName().equals(LicenseProduct.REMOTE.toString())) {
					remotePlanCount = licenseProductInfoResponse.getUseLicenseAmount();
				}
				if (licenseProductInfoResponse.getProductName().equals(LicenseProduct.MAKE.toString())) {
					makePlanCount = licenseProductInfoResponse.getUseLicenseAmount();
				}
				if (licenseProductInfoResponse.getProductName().equals(LicenseProduct.VIEW.toString())) {
					viewPlanCount = licenseProductInfoResponse.getUseLicenseAmount();
				}
			}
		}
		return new WorkspaceInfoResponse(
			workspaceInfo, userInfoList, masterUserCount, managerUserCount, memberUserCount, remotePlanCount,
			makePlanCount, viewPlanCount
		);
	}

	/**
	 * 워크스페이스 정보 조회
	 *
	 * @param workspaceId - 워크스페이스 식별자
	 * @return - 워크스페이스 정보
	 */
	public WorkspaceInfoDTO getWorkspaceInfo(String workspaceId) {
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		return workspaceMapStruct.workspaceToWorkspaceInfoDTO(workspace);
	}

	/**
	 * 유저 정보 조회(User Service)
	 *
	 * @param userId - 유저 uuid
	 * @return - 유저 정보
	 */
	UserInfoRestResponse getUserInfo(String userId) {
		ApiResponse<UserInfoRestResponse> userInfoResponse = userRestService.getUserInfoByUserId(userId);
		if (userInfoResponse.getCode() != 200 || userInfoResponse.getData() == null) {
			log.error(
				"[GET USER INFO] request userId : {}, response code : {}, response message : {}", userId,
				userInfoResponse.getCode(), userInfoResponse.getMessage()
			);
			//throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
			return null;
		}
		return userInfoResponse.getData();
	}

	/**
	 * 워크스페이스 정보 변경
	 *
	 * @param workspaceUpdateRequest
	 * @param locale
	 * @return
	 */
	public WorkspaceInfoDTO setWorkspace(WorkspaceUpdateRequest workspaceUpdateRequest, Locale locale) {
		if (!StringUtils.hasText(workspaceUpdateRequest.getUserId()) || !StringUtils.hasText(
			workspaceUpdateRequest.getName())
			|| !StringUtils.hasText(workspaceUpdateRequest.getDescription()) || !StringUtils.hasText(
			workspaceUpdateRequest.getWorkspaceId())) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}

		//마스터 유저 체크
		Workspace workspace = workspaceRepository.findByUuid(workspaceUpdateRequest.getWorkspaceId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		String oldWorkspaceName = workspace.getName();
		if (!workspace.getUserId().equals(workspaceUpdateRequest.getUserId())) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		//이름이 변경된 경우에만 메일 전송
		if (!oldWorkspaceName.equals(workspaceUpdateRequest.getName())) {
			List<String> receiverEmailList = new ArrayList<>();
			List<WorkspaceUser> workspaceUserList = workspaceUserRepository.findByWorkspace_Uuid(workspace.getUuid());
			workspaceUserList.forEach(workspaceUser -> {
				//applicationEventPublisher.publishEvent(new UserWorkspacesDeleteEvent(workspaceUser.getUserId()));//캐싱 삭제
				UserInfoRestResponse userInfoRestResponse = getUserInfo(workspaceUser.getUserId());
				if (userInfoRestResponse != null) {
					receiverEmailList.add(userInfoRestResponse.getEmail());
				}
			});
			UserInfoRestResponse masterUserInfo = getUserInfo(workspace.getUserId());
			Context context = mailContextHandler.getWorkspaceInfoUpdateContext(
				oldWorkspaceName, workspaceUpdateRequest.getName(), masterUserInfo);
			applicationEventPublisher.publishEvent(
				new MailSendEvent(context, Mail.WORKSPACE_INFO_UPDATE, locale, receiverEmailList));
		}
		workspace.setName(workspaceUpdateRequest.getName());
		workspace.setDescription(workspaceUpdateRequest.getDescription());

		if (workspaceUpdateRequest.getProfile() != null) {
			String oldProfile = workspace.getProfile();
			//기존 프로필 이미지 삭제
			if (StringUtils.hasText(oldProfile)) {
				fileUploadService.delete(oldProfile);
			}
			//새 프로필 이미지 등록
			try {
				workspace.setProfile(fileUploadService.upload(
					workspaceUpdateRequest.getProfile(),
					workspaceUpdateRequest.getWorkspaceId()
				));
			} catch (Exception e) {
				throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
			}

		}

		workspaceRepository.save(workspace);

		return workspaceMapStruct.workspaceToWorkspaceInfoDTO(workspace);
	}

	public WorkspaceLicenseInfoResponse getWorkspaceLicenseInfo(String workspaceId) {
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		/*if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null) {
			throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
		}*/

		WorkspaceLicenseInfoResponse workspaceLicenseInfoResponse = new WorkspaceLicenseInfoResponse();
		workspaceLicenseInfoResponse.setLicenseInfoList(new ArrayList<>());

		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() != null
			&& !workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			List<WorkspaceLicenseInfoResponse.LicenseInfo> licenseInfoList = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
				.stream()
				.map(licenseProductInfoResponse -> {
					WorkspaceLicenseInfoResponse.LicenseInfo licenseInfo = new WorkspaceLicenseInfoResponse.LicenseInfo();
					licenseInfo.setLicenseType(licenseProductInfoResponse.getLicenseType());
					licenseInfo.setProductName(licenseProductInfoResponse.getProductName());
					licenseInfo.setUseLicenseAmount(licenseProductInfoResponse.getUseLicenseAmount());
					licenseInfo.setLicenseAmount(licenseProductInfoResponse.getUnUseLicenseAmount()
						+ licenseProductInfoResponse.getUseLicenseAmount());
					return licenseInfo;
				})
				.collect(Collectors.toList());
			workspaceLicenseInfoResponse.setLicenseInfoList(licenseInfoList);
		}

		DecimalFormat decimalFormat = new DecimalFormat("0");
		long size = workspaceLicensePlanInfoResponse.getMaxStorageSize();
		workspaceLicenseInfoResponse.setMaxStorageSize(Long.parseLong(decimalFormat.format(size / 1024.0))); //MB -> GB
		workspaceLicenseInfoResponse.setMaxDownloadHit(workspaceLicensePlanInfoResponse.getMaxDownloadHit());
		workspaceLicenseInfoResponse.setMaxCallTime(workspaceLicenseInfoResponse.getMaxCallTime());

		return workspaceLicenseInfoResponse;
	}

	/***
	 * 워크스페이스 정보 전체 삭제 처리
	 * @param workspaceUUID - 삭제할 워크스페이스의 마스터 사용자 식별자
	 * @return - 삭제 처리 결과
	 */
	@Transactional
	public WorkspaceSecessionResponse deleteAllWorkspaceInfo(String userUUID) {
		List<WorkspaceUserPermission> workspaceUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_UserId(
			userUUID);
		workspaceUserPermissionList.forEach(workspaceUserPermission -> {
			if (workspaceUserPermission.getWorkspaceRole().getRole() == Role.MASTER) {
				Workspace workspace = workspaceUserPermission.getWorkspaceUser().getWorkspace();

				List<WorkspaceUser> workspaceUserList = workspace.getWorkspaceUserList();

				// workspace user permission 삭제
				workspaceUserPermissionRepository.deleteAllWorkspaceUserPermissionByWorkspaceUser(workspaceUserList);

				// workspace user 삭제
				workspaceUserRepository.deleteAllWorkspaceUserByWorkspace(workspace);

				// workspace history 삭제
				historyRepository.deleteAllHistoryInfoByWorkspace(workspace);

				// workspace profile 삭제 (기본 이미지인 경우 제외)
				fileUploadService.delete(workspace.getProfile());

				// workspace 삭제
				workspaceRepository.delete(workspace);
			} else {
				WorkspaceUser workspaceUser = workspaceUserPermission.getWorkspaceUser();

				// workspace user permission 삭제
				workspaceUserPermissionRepository.delete(workspaceUserPermission);
				// workspace user 삭제
				workspaceUserRepository.delete(workspaceUser);
			}
		});
		return new WorkspaceSecessionResponse(userUUID, true, LocalDateTime.now());
	}

	@Transactional
	public abstract WorkspaceTitleUpdateResponse updateWorkspaceTitle(
		String workspaceId, WorkspaceTitleUpdateRequest workspaceTitleUpdateRequest
	);

	@Transactional
	public abstract WorkspaceLogoUpdateResponse updateWorkspaceLogo(
		String workspaceId, WorkspaceLogoUpdateRequest workspaceLogoUpdateRequest
	);

	@Transactional
	public abstract WorkspaceFaviconUpdateResponse updateWorkspaceFavicon(
		String workspaceId, WorkspaceFaviconUpdateRequest workspaceFaviconUpdateRequest
	);

	public abstract WorkspaceCustomSettingResponse getWorkspaceCustomSetting(String workspaceId);

	@Transactional
	public abstract WorkspaceRemoteLogoUpdateResponse updateRemoteLogo(String workspaceId, WorkspaceRemoteLogoUpdateRequest remoteLogoUpdateRequest);

	public String getRemoteLogoUrl(
		DefaultImageName defaultImageName, boolean isDefaultLogo, MultipartFile file,
		String workspaceId
	) {
		if (isDefaultLogo) {
			return null;
		}
		int dotIndex = defaultImageName.getName().indexOf(".");
		String fixedFileName = defaultImageName.getName().substring(0, dotIndex);
		return fileUploadService.uploadByFixedName(file, workspaceId, fixedFileName);
	}

	public String getLogoUrl(
		MultipartFile file, String workspaceId
	) {
		if (file == null || file.getSize() == 0) {
			return null;
		}
		return fileUploadService.upload(file, workspaceId);
	}

}
