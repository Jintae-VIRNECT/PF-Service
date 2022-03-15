package com.virnect.workspace.application.workspace;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.application.license.LicenseRestService;
import com.virnect.workspace.application.user.UserRestService;
import com.virnect.workspace.application.user.dto.response.UserInfoRestResponse;
import com.virnect.workspace.dao.history.HistoryRepository;
import com.virnect.workspace.dao.setting.WorkspaceSettingRepository;
import com.virnect.workspace.dao.workspace.WorkspaceRepository;
import com.virnect.workspace.dao.workspacepermission.WorkspacePermissionRepository;
import com.virnect.workspace.dao.workspacerole.WorkspaceRoleRepository;
import com.virnect.workspace.dao.workspaceuser.WorkspaceUserRepository;
import com.virnect.workspace.dao.workspaceuserpermission.WorkspaceUserPermissionRepository;
import com.virnect.workspace.domain.workspace.Role;
import com.virnect.workspace.domain.workspace.Workspace;
import com.virnect.workspace.domain.workspace.WorkspacePermission;
import com.virnect.workspace.domain.workspace.WorkspaceRole;
import com.virnect.workspace.domain.workspace.WorkspaceSetting;
import com.virnect.workspace.domain.workspace.WorkspaceUser;
import com.virnect.workspace.domain.workspace.WorkspaceUserPermission;
import com.virnect.workspace.dto.request.WorkspaceCreateRequest;
import com.virnect.workspace.dto.request.WorkspaceFaviconUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceLogoUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceRemoteLogoUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceTitleUpdateRequest;
import com.virnect.workspace.dto.response.WorkspaceCustomSettingResponse;
import com.virnect.workspace.dto.response.WorkspaceFaviconUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoDTO;
import com.virnect.workspace.dto.response.WorkspaceLogoUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceRemoteLogoUpdateResponse;
import com.virnect.workspace.dto.response.WorkspaceTitleUpdateResponse;
import com.virnect.workspace.event.message.MailContextHandler;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.mapper.rest.RestMapStruct;
import com.virnect.workspace.global.common.mapper.workspace.WorkspaceMapStruct;
import com.virnect.workspace.global.constant.Permission;
import com.virnect.workspace.global.constant.UUIDType;
import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.global.util.RandomStringTokenUtil;
import com.virnect.workspace.infra.file.DefaultImageName;
import com.virnect.workspace.infra.file.FileService;

/**
 * Project: PF-Workspace
 * DATE: 2021-02-02
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Service
@Profile("onpremise")
public class OffWorkspaceServiceImpl extends WorkspaceService {
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final FileService fileUploadService;
	private final WorkspaceUserRepository workspaceUserRepository;
	private final WorkspaceSettingRepository workspaceSettingRepository;
	private final WorkspaceRoleRepository workspaceRoleRepository;
	private final WorkspaceMapStruct workspaceMapStruct;
	private final ApplicationEventPublisher applicationEventPublisher;
	private final WorkspacePermissionRepository workspacePermissionRepository;

	public OffWorkspaceServiceImpl(
		WorkspaceRepository workspaceRepository, WorkspaceUserRepository workspaceUserRepository,
		WorkspaceUserPermissionRepository workspaceUserPermissionRepository, UserRestService userRestService,
		FileService fileUploadService, HistoryRepository historyRepository, LicenseRestService licenseRestService,
		WorkspaceMapStruct workspaceMapStruct, RestMapStruct restMapStruct,
		ApplicationEventPublisher applicationEventPublisher, MailContextHandler mailContextHandler,
		WorkspaceSettingRepository workspaceSettingRepository, WorkspaceRoleRepository workspaceRoleRepository,
		WorkspacePermissionRepository workspacePermissionRepository
	) {
		super(
			workspaceRepository, workspaceUserRepository, workspaceUserPermissionRepository, userRestService,
			fileUploadService, historyRepository, licenseRestService, workspaceMapStruct, restMapStruct,
			applicationEventPublisher, mailContextHandler,
			workspaceSettingRepository
		);
		this.workspaceRepository = workspaceRepository;
		this.workspaceUserPermissionRepository = workspaceUserPermissionRepository;
		this.fileUploadService = fileUploadService;
		this.workspaceUserRepository = workspaceUserRepository;
		this.workspaceSettingRepository = workspaceSettingRepository;
		this.workspaceRoleRepository = workspaceRoleRepository;
		this.workspaceMapStruct = workspaceMapStruct;
		this.applicationEventPublisher = applicationEventPublisher;
		this.workspacePermissionRepository = workspacePermissionRepository;
	}

	private static final int MAX_HAVE_WORKSPACE_AMOUNT = 1; //최대 생성 가능한 워크스페이스 수

	@Override
	public WorkspaceInfoDTO createWorkspace(WorkspaceCreateRequest workspaceCreateRequest) {
		//1. 서브유저(유저가 만들어낸 유저)는 워크스페이스를 가질 수 없다.
		UserInfoRestResponse userInfoRestResponse = super.getUserInfo(workspaceCreateRequest.getUserId());
		if (userInfoRestResponse.isSubUser()) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		//2. 사용자가 생성 가능한 워크스페이스 수를 넘겼는지 체크
		long userHasWorkspaceAmount = workspaceRepository.countByUserId(workspaceCreateRequest.getUserId());
		if (userHasWorkspaceAmount + 1 > MAX_HAVE_WORKSPACE_AMOUNT) {
			log.error(
				"[WORKSPACE CREATE] creatable maximum Workspace amount : [{}], current amount of workspace that user has : [{}].",
				MAX_HAVE_WORKSPACE_AMOUNT, userHasWorkspaceAmount
			);
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_CREATE_MAX_CREATE);
		}

		String workspaceId = RandomStringTokenUtil.generate(UUIDType.WORKSPACE_UUID, 0);

		String profile = null;
		if (workspaceCreateRequest.getProfile() == null) {
			profile = fileUploadService.getDefaultFileUrl(DefaultImageName.WORKSPACE_PROFILE);
		} else {
			profile = fileUploadService.upload(workspaceCreateRequest.getProfile(), workspaceId);
		}

		// 워크스페이스 생성
		Workspace workspace = Workspace.builder()
			.uuid(workspaceId)
			.userId(workspaceCreateRequest.getUserId())
			.name(workspaceCreateRequest.getName())
			.description(workspaceCreateRequest.getDescription())
			.profile(profile)
			.pinNumber(RandomStringTokenUtil.generate(UUIDType.PIN_NUMBER, 0))
			.build();
		workspaceRepository.save(workspace);

		// 워크스페이스 소속 할당
		WorkspaceUser newWorkspaceUser = WorkspaceUser.builder()
			.userId(workspaceCreateRequest.getUserId())
			.workspace(workspace)
			.build();
		workspaceUserRepository.save(newWorkspaceUser);

		// 워크스페이스 권한 할당
		WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(Role.MASTER)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_ROLE_NOT_FOUND));
		WorkspacePermission workspacePermission = workspacePermissionRepository.findByPermission(Permission.ALL)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_PERMISSION_NOT_FOUND));
		WorkspaceUserPermission workspaceUserPermission = WorkspaceUserPermission.builder()
			.workspaceRole(workspaceRole)
			.workspacePermission(workspacePermission)
			.workspaceUser(newWorkspaceUser)
			.build();
		workspaceUserPermissionRepository.save(workspaceUserPermission);

		return workspaceMapStruct.workspaceToWorkspaceInfoDTO(workspace);
	}

	@Transactional
	@Override
	public WorkspaceTitleUpdateResponse updateWorkspaceTitle(
		String workspaceId, WorkspaceTitleUpdateRequest workspaceTitleUpdateRequest
	) {
		WorkspaceUserPermission workspaceUser = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, workspaceTitleUpdateRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		if (!Role.MASTER.equals(workspaceUser.getWorkspaceRole().getRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		WorkspaceSetting workspaceSetting = getFirstWorkspaceSetting(workspaceId);


		workspaceSetting.setTitle(workspaceTitleUpdateRequest.getTitle());
		workspaceSettingRepository.save(workspaceSetting);

		WorkspaceTitleUpdateResponse workspaceTitleUpdateResponse = new WorkspaceTitleUpdateResponse();
		workspaceTitleUpdateResponse.setResult(true);
		workspaceTitleUpdateResponse.setTitle(workspaceSetting.getTitle());
		return workspaceTitleUpdateResponse;
	}

	@Transactional
	@Override
	public WorkspaceLogoUpdateResponse updateWorkspaceLogo(
		String workspaceId, WorkspaceLogoUpdateRequest workspaceLogoUpdateRequest
	) {
		WorkspaceUserPermission workspaceUser = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, workspaceLogoUpdateRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		if (!Role.MASTER.equals(workspaceUser.getWorkspaceRole().getRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		WorkspaceSetting workspaceSetting = getFirstWorkspaceSetting(workspaceId);


		String defaultLogo = getLogoUrl(workspaceLogoUpdateRequest.getDefaultLogo(), workspaceId);
		workspaceSetting.setDefaultLogo(defaultLogo);

		String whiteLogo = getLogoUrl(workspaceLogoUpdateRequest.getWhiteLogo(), workspaceId);
		workspaceSetting.setWhiteLogo(whiteLogo);

		workspaceSettingRepository.save(workspaceSetting);

		WorkspaceLogoUpdateResponse workspaceLogoUpdateResponse = new WorkspaceLogoUpdateResponse();
		workspaceLogoUpdateResponse.setResult(true);
		workspaceLogoUpdateResponse.setDefaultLogo(workspaceSetting.getDefaultLogo());
		workspaceLogoUpdateResponse.setGreyLogo(workspaceSetting.getGreyLogo());
		workspaceLogoUpdateResponse.setWhiteLogo(workspaceSetting.getWhiteLogo());
		return workspaceLogoUpdateResponse;

	}

	@Transactional
	@Override
	public WorkspaceFaviconUpdateResponse updateWorkspaceFavicon(
		String workspaceId, WorkspaceFaviconUpdateRequest workspaceFaviconUpdateRequest
	)  {
		WorkspaceUserPermission workspaceUser = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, workspaceFaviconUpdateRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		if (!Role.MASTER.equals(workspaceUser.getWorkspaceRole().getRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		WorkspaceSetting workspaceSetting = getFirstWorkspaceSetting(workspaceId);

		String favicon = getLogoUrl(workspaceFaviconUpdateRequest.getFavicon(), workspaceId);
		workspaceSetting.setFavicon(favicon);
		workspaceSettingRepository.save(workspaceSetting);

		WorkspaceFaviconUpdateResponse workspaceFaviconUpdateResponse = new WorkspaceFaviconUpdateResponse();
		workspaceFaviconUpdateResponse.setResult(true);
		workspaceFaviconUpdateResponse.setFavicon(favicon);
		return workspaceFaviconUpdateResponse;
	}

	@Override
	public WorkspaceCustomSettingResponse getWorkspaceCustomSetting(String workspaceId) {
		// onpremise는 단일 워크스페이스.
		WorkspaceSetting workspaceSetting = workspaceSettingRepository.findAll().stream().findFirst()
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));

		return workspaceMapStruct.workspaceSettingToWorkspaceCustomSettingResponse(workspaceSetting);
	}

	@Transactional
	@Override
	public WorkspaceRemoteLogoUpdateResponse updateRemoteLogo(
		String workspaceId, WorkspaceRemoteLogoUpdateRequest remoteLogoUpdateRequest
	) {
		WorkspaceUserPermission workspaceUser = workspaceUserPermissionRepository.findWorkspaceUserPermission(
				workspaceId, remoteLogoUpdateRequest.getUserId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));
		if (!Role.MASTER.equals(workspaceUser.getWorkspaceRole().getRole())) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		WorkspaceSetting workspaceSetting = getFirstWorkspaceSetting(workspaceId);

		if (remoteLogoUpdateRequest.isUpdateAndroidSplashLogo()) {
			String logoUrl = getRemoteLogoUrl(
				DefaultImageName.REMOTE_ANDROID_SPLASH_LOGO,
				remoteLogoUpdateRequest.isDefaultRemoteAndroidSplashLogo(),
				remoteLogoUpdateRequest.getRemoteAndroidSplashLogo(), workspaceId
			);
			workspaceSetting.setRemoteAndroidSplashLogo(logoUrl);
		}

		if (remoteLogoUpdateRequest.isUpdateAndroidLoginLogo()) {
			String logoUrl = getRemoteLogoUrl(
				DefaultImageName.REMOTE_ANDROID_LOGIN_LOGO,
				remoteLogoUpdateRequest.isDefaultRemoteAndroidLoginLogo(),
				remoteLogoUpdateRequest.getRemoteAndroidLoginLogo(), workspaceId
			);
			workspaceSetting.setRemoteAndroidLoginLogo(logoUrl);
		}

		if (remoteLogoUpdateRequest.isUpdateHololens2Logo()) {
			String logoUrl = getRemoteLogoUrl(
				DefaultImageName.REMOTE_HOLOLENS2_COMMON_LOGO,
				remoteLogoUpdateRequest.isDefaultRemoteHololens2CommonLogo(),
				remoteLogoUpdateRequest.getRemoteHololens2CommonLogo(), workspaceId
			);
			workspaceSetting.setRemoteHololens2CommonLogo(logoUrl);
		}
		workspaceSettingRepository.save(workspaceSetting);

		return new WorkspaceRemoteLogoUpdateResponse(true, workspaceSetting.getRemoteAndroidSplashLogo(),
			workspaceSetting.getRemoteAndroidLoginLogo(),
			workspaceSetting.getRemoteHololens2CommonLogo()
		);
	}

	private WorkspaceSetting getFirstWorkspaceSetting(String workspaceId) {
		Optional<WorkspaceSetting> optionalWorkspaceSetting = workspaceSettingRepository.findAll().stream().findFirst();
		return optionalWorkspaceSetting.orElseGet(
			() -> WorkspaceSetting.workspaceSettingInitBuilder().workspaceId(workspaceId).build());

	}
}
