package com.virnect.workspace.application;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.workspace.dao.HistoryRepository;
import com.virnect.workspace.dao.WorkspacePermissionRepository;
import com.virnect.workspace.dao.WorkspaceRepository;
import com.virnect.workspace.dao.WorkspaceRoleRepository;
import com.virnect.workspace.dao.WorkspaceUserPermissionRepository;
import com.virnect.workspace.dao.WorkspaceUserRepository;
import com.virnect.workspace.dao.redis.UserInviteRepository;
import com.virnect.workspace.domain.History;
import com.virnect.workspace.domain.Workspace;
import com.virnect.workspace.domain.WorkspacePermission;
import com.virnect.workspace.domain.WorkspaceRole;
import com.virnect.workspace.domain.WorkspaceUser;
import com.virnect.workspace.domain.WorkspaceUserPermission;
import com.virnect.workspace.domain.redis.UserInvite;
import com.virnect.workspace.domain.rest.LicenseProductStatus;
import com.virnect.workspace.domain.rest.LicenseStatus;
import com.virnect.workspace.dto.MemberInfoDTO;
import com.virnect.workspace.dto.UserInfoDTO;
import com.virnect.workspace.dto.WorkspaceInfoDTO;
import com.virnect.workspace.dto.WorkspaceNewMemberInfoDTO;
import com.virnect.workspace.dto.onpremise.MemberAccountCreateInfo;
import com.virnect.workspace.dto.onpremise.MemberAccountCreateRequest;
import com.virnect.workspace.dto.onpremise.WorkspaceCustomSettingResponse;
import com.virnect.workspace.dto.onpremise.WorkspaceLogoUpdateRequest;
import com.virnect.workspace.dto.onpremise.WorkspaceLogoUpdateResponse;
import com.virnect.workspace.dto.onpremise.WorkspacePaviconUpdateRequest;
import com.virnect.workspace.dto.onpremise.WorkspacePaviconUpdateResponse;
import com.virnect.workspace.dto.onpremise.WorkspaceTitleUpdateRequest;
import com.virnect.workspace.dto.onpremise.WorkspaceTitleUpdateResponse;
import com.virnect.workspace.dto.request.MemberAccountDeleteRequest;
import com.virnect.workspace.dto.request.MemberKickOutRequest;
import com.virnect.workspace.dto.request.MemberUpdateRequest;
import com.virnect.workspace.dto.request.WorkspaceCreateRequest;
import com.virnect.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.workspace.dto.request.WorkspaceUpdateRequest;
import com.virnect.workspace.dto.response.MemberListResponse;
import com.virnect.workspace.dto.response.WorkspaceHistoryListResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceLicenseInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceMemberInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceSecessionResponse;
import com.virnect.workspace.dto.response.WorkspaceUserLicenseInfoResponse;
import com.virnect.workspace.dto.response.WorkspaceUserLicenseListResponse;
import com.virnect.workspace.dto.rest.InviteUserInfoRestResponse;
import com.virnect.workspace.dto.rest.MailRequest;
import com.virnect.workspace.dto.rest.MyLicenseInfoListResponse;
import com.virnect.workspace.dto.rest.MyLicenseInfoResponse;
import com.virnect.workspace.dto.rest.PageMetadataRestResponse;
import com.virnect.workspace.dto.rest.RegisterMemberRequest;
import com.virnect.workspace.dto.rest.UserDeleteRestResponse;
import com.virnect.workspace.dto.rest.UserInfoAccessCheckRequest;
import com.virnect.workspace.dto.rest.UserInfoAccessCheckResponse;
import com.virnect.workspace.dto.rest.UserInfoListRestResponse;
import com.virnect.workspace.dto.rest.UserInfoRestResponse;
import com.virnect.workspace.dto.rest.WorkspaceLicensePlanInfoResponse;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.constant.LicenseProduct;
import com.virnect.workspace.global.constant.Mail;
import com.virnect.workspace.global.constant.MailSender;
import com.virnect.workspace.global.constant.Permission;
import com.virnect.workspace.global.constant.RedirectPath;
import com.virnect.workspace.global.constant.Role;
import com.virnect.workspace.global.constant.UUIDType;
import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.global.util.RandomStringTokenUtil;
import com.virnect.workspace.infra.file.FileService;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceUserRepository workspaceUserRepository;
	private final WorkspaceRoleRepository workspaceRoleRepository;
	private final WorkspacePermissionRepository workspacePermissionRepository;
	private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
	private final UserRestService userRestService;
	private final ModelMapper modelMapper;
	private final MessageRestService messageRestService;
	private final ProcessRestService processRestService;
	private final FileService fileUploadService;
	private final UserInviteRepository userInviteRepository;
	private final SpringTemplateEngine springTemplateEngine;
	private final HistoryRepository historyRepository;
	private final MessageSource messageSource;
	private final LicenseRestService licenseRestService;

	@Value("${serverUrl}")
	private String serverUrl;

	@Value("${redirectUrl}")
	private String redirectUrl;

	@Value("${contactUrl}")
	private String contactUrl;

	@Value("${accountUrl}")
	private String accountUrl;

	@Value("${supportUrl}")
	private String supportUrl;

	private static final String serviceID = "workspace-server";

	/**
	 * 워크스페이스 생성
	 *
	 * @param workspaceCreateRequest - 생성 할 워크스페이스 정보
	 * @return - 생성 된 워크스페이스 정보
	 */
	public ApiResponse<WorkspaceInfoDTO> createWorkspace(WorkspaceCreateRequest workspaceCreateRequest) {
		//필수 값 체크
		if (!StringUtils.hasText(workspaceCreateRequest.getUserId()) || !StringUtils.hasText(
			workspaceCreateRequest.getName()) || !StringUtils.hasText(workspaceCreateRequest.getDescription())) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}

		//User Service 에서 유저 조회
		UserInfoRestResponse userInfoRestResponse = getUserInfo(workspaceCreateRequest.getUserId());

		//서브유저(유저가 만들어낸 유저)는 워크스페이스를 가질 수 없다.
		if (userInfoRestResponse.getUserType().equals("SUB_USER")) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		//이미 생성한 워크스페이스가 있는지 확인(사용자가 마스터로 소속되는 워크스페이스는 단 1개다.)
		boolean userHasWorkspace = this.workspaceRepository.existsByUserId(workspaceCreateRequest.getUserId());

		if (userHasWorkspace) {
			throw new WorkspaceException(ErrorCode.ERR_MASTER_WORKSPACE_ALREADY_EXIST);
		}
		//워크스페이스 생성
		String uuid = RandomStringTokenUtil.generate(UUIDType.UUID_WITH_SEQUENCE, 0);
		String pinNumber = RandomStringTokenUtil.generate(UUIDType.PIN_NUMBER, 0);

		String profile = null;
		if (workspaceCreateRequest.getProfile() != null) {
			try {
				profile = this.fileUploadService.upload(workspaceCreateRequest.getProfile());
			} catch (IOException e) {
				throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
			}
		} else {
			profile = this.fileUploadService.getFileUrl("workspace-profile.png");
		}

		Workspace newWorkspace = Workspace.builder()
			.uuid(uuid)
			.userId(workspaceCreateRequest.getUserId())
			.name(workspaceCreateRequest.getName())
			.description(workspaceCreateRequest.getDescription())
			.profile(profile)
			.pinNumber(pinNumber)
			.build();

		this.workspaceRepository.save(newWorkspace);

		// 워크스페이스 소속 할당
		WorkspaceUser newWorkspaceUser = WorkspaceUser.builder()
			.userId(workspaceCreateRequest.getUserId())
			.workspace(newWorkspace)
			.build();
		this.workspaceUserRepository.save(newWorkspaceUser);

		// 워크스페이스 권한 할당
		WorkspaceRole workspaceRole = WorkspaceRole.builder().id(Role.MASTER.getValue()).build();
		WorkspacePermission workspacePermission = WorkspacePermission.builder().id(Permission.ALL.getValue()).build();
		WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
			.workspaceRole(workspaceRole)
			.workspacePermission(workspacePermission)
			.workspaceUser(newWorkspaceUser)
			.build();
		this.workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

		WorkspaceInfoDTO workspaceInfoDTO = modelMapper.map(newWorkspace, WorkspaceInfoDTO.class);
		workspaceInfoDTO.setId(newWorkspace.getId());
		workspaceInfoDTO.setMasterUserId(newWorkspace.getUserId());
		return new ApiResponse<>(workspaceInfoDTO);
	}

	/**
	 * 사용자 소속 워크스페이스 조회
	 *
	 * @param userId - 사용자 uuid
	 * @return - 소속된 워크스페이스 정보
	 */
	public ApiResponse<WorkspaceInfoListResponse> getUserWorkspaces(
		String userId, com.virnect.workspace.global.common.PageRequest pageRequest
	) {
		String sortName = pageRequest.of().getSort().toString().split(":")[0].trim();
		String sortDirection = pageRequest.of().getSort().toString().split(":")[1].trim();

		if (sortName.equalsIgnoreCase("role")) {
			sortName = "workspaceRole";
			String sort = sortName + "," + sortDirection;
			pageRequest.setSort(sort);
		}

		Pageable pageable = pageRequest.of();
		List<WorkspaceInfoListResponse.WorkspaceInfo> workspaceList = new ArrayList<>();

		Page<WorkspaceUserPermission> workspaceUserPermissionPage = this.workspaceUserPermissionRepository.findByWorkspaceUser_UserId(
			userId, pageable);

		for (WorkspaceUserPermission workspaceUserPermission : workspaceUserPermissionPage) {

			WorkspaceUser workspaceUser = workspaceUserPermission.getWorkspaceUser();
			Workspace workspace = workspaceUser.getWorkspace();

			WorkspaceInfoListResponse.WorkspaceInfo workspaceInfo = modelMapper.map(
				workspace, WorkspaceInfoListResponse.WorkspaceInfo.class);
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
		pageMetadataResponse.setCurrentPage(pageable.getPageNumber());
		pageMetadataResponse.setCurrentSize(pageable.getPageSize());

		return new ApiResponse<>(new WorkspaceInfoListResponse(workspaceList, pageMetadataResponse));
	}

	/**
	 * 멤버 조회
	 *
	 * @param workspaceId - 조회 대상 workspace uuid
	 * @param userId      - 조회 중인 유저 uuid
	 * @param search      - (옵션) 검색값
	 * @param filter      - (옵션) 필터값 ex.MEMBER, MASTER
	 * @param pageable    - 페이징 처리 값
	 * @return - 멤버 정보 리스트
	 */
	public ApiResponse<MemberListResponse> getMembers(
		String workspaceId, String search, String filter, com.virnect.workspace.global.common.PageRequest pageRequest
	) {

		//Pageable로 Sort처리를 할 수 없기때문에 sort값을 제외한 Pageable을 만든다.
		Pageable newPageable = PageRequest.of(pageRequest.of().getPageNumber(), pageRequest.of().getPageSize());

		//filter set
		List<WorkspaceRole> workspaceRoleList = new ArrayList<>();
		if (StringUtils.hasText(filter) && filter.toUpperCase().contains("MASTER")) {
			workspaceRoleList.add(WorkspaceRole.builder().id(1L).build());
		}
		if (StringUtils.hasText(filter) && filter.toUpperCase().contains("MANAGER")) {
			workspaceRoleList.add(WorkspaceRole.builder().id(2L).build());
		}
		if (StringUtils.hasText(filter) && filter.toUpperCase().contains("MEMBER")) {
			workspaceRoleList.add(WorkspaceRole.builder().id(3L).build());
		}
		List<String> licenseProductList = new ArrayList<>();
		if (StringUtils.hasText(filter) && filter.toUpperCase().contains("REMOTE")) {
			licenseProductList.add("REMOTE");
		}
		if (StringUtils.hasText(filter) && filter.toUpperCase().contains("MAKE")) {
			licenseProductList.add("MAKE");
		}
		if (StringUtils.hasText(filter) && filter.toUpperCase().contains("VIEW")) {
			licenseProductList.add("VIEW");
		}

		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

		//USER-SERVER : 워크스페이스에 해당하는 유저들에 대한 정보만 불러온다. (+ search)
		List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId);
		String[] workspaceUserIdList = workspaceUserList.stream()
			.map(workspaceUser -> workspaceUser.getUserId())
			.toArray(String[]::new);

		List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();
		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();

		UserInfoListRestResponse userInfoListRestResponse = this.userRestService.getUserInfoList(
			search, workspaceUserIdList).getData();

		//filter로 role을 건 경우
		if (!workspaceRoleList.isEmpty()) {
			List<WorkspaceUser> workspaceUsers = userInfoListRestResponse.getUserInfoList()
				.stream()
				.map(userInfoRestResponse -> workspaceUserRepository.findByUserIdAndWorkspace(
					userInfoRestResponse.getUuid(), workspace))
				.collect(Collectors.toList());
			Page<WorkspaceUserPermission> workspaceUserPermissionPage = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUserIsInAndWorkspaceRoleIsIn(
				workspace, workspaceUsers, workspaceRoleList, newPageable);
			List<WorkspaceUserPermission> filterdWorkspaceUserList = workspaceUserPermissionPage.toList();

			for (UserInfoRestResponse userInfoRestResponse : userInfoListRestResponse.getUserInfoList()) {
				WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
					workspace, userInfoRestResponse.getUuid());
				if (filterdWorkspaceUserList.contains(workspaceUserPermission)) {
					MemberInfoDTO memberInfoDTO = this.modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
					memberInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
					memberInfoDTO.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());
					memberInfoDTO.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());

					String[] licenseProducts = new String[0];
					MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
						workspaceId, userInfoRestResponse.getUuid()).getData();
					if (myLicenseInfoListResponse.getLicenseInfoList() != null
						&& !myLicenseInfoListResponse.getLicenseInfoList().isEmpty()) {
						licenseProducts = myLicenseInfoListResponse.getLicenseInfoList()
							.stream()
							.map(myLicenseInfoResponse -> myLicenseInfoResponse.getProductName())
							.toArray(String[]::new);
						memberInfoDTO.setLicenseProducts(licenseProducts);
					}
					memberInfoDTO.setLicenseProducts(licenseProducts);
					memberInfoDTOList.add(memberInfoDTO);
				}
			}
			pageMetadataResponse.setTotalElements(workspaceUserPermissionPage.getTotalElements());
			pageMetadataResponse.setTotalPage(workspaceUserPermissionPage.getTotalPages());
			pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
			pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

			List<MemberInfoDTO> resultMemberListResponse = getSortedMemberList(pageRequest, memberInfoDTOList);
			return new ApiResponse<>(new MemberListResponse(resultMemberListResponse, pageMetadataResponse));

		}
		//filter로 license 정보를 건 경우
		if (!licenseProductList.isEmpty()) {
			List<String> userIdList = new ArrayList<>();
			userInfoListRestResponse.getUserInfoList().forEach(userInfoRestResponse -> {
				MyLicenseInfoListResponse userLicenseInfo = licenseRestService.getMyLicenseInfoRequestHandler(
					workspaceId, userInfoRestResponse.getUuid()).getData();
				if (userLicenseInfo.getLicenseInfoList() != null && !userLicenseInfo.getLicenseInfoList().isEmpty()) {
					userLicenseInfo.getLicenseInfoList().forEach(myLicenseInfoResponse -> {
						if (licenseProductList.contains(myLicenseInfoResponse.getProductName()) && !userIdList.contains(
							userInfoRestResponse.getUuid())) {
							//페이징 만들 데이터 넣기
							userIdList.add(userInfoRestResponse.getUuid());
						}
					});
				}
			});
			Page<WorkspaceUser> workspaceUserPage = this.workspaceUserRepository.findByWorkspace_UuidAndUserIdIn(
				workspaceId, userIdList, newPageable);
			List<WorkspaceUser> resultWorkspaceUser = workspaceUserPage.toList();

			for (UserInfoRestResponse userInfoRestResponse : userInfoListRestResponse.getUserInfoList()) {

				WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
					workspace, userInfoRestResponse.getUuid());
				if (resultWorkspaceUser.contains(workspaceUserPermission.getWorkspaceUser())) {
					MemberInfoDTO memberInfoDTO = this.modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
					memberInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
					memberInfoDTO.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());
					memberInfoDTO.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());

					String[] licenseProducts = new String[0];
					MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
						workspaceId, userInfoRestResponse.getUuid()).getData();
					if (myLicenseInfoListResponse.getLicenseInfoList() != null
						&& !myLicenseInfoListResponse.getLicenseInfoList().isEmpty()) {
						licenseProducts = myLicenseInfoListResponse.getLicenseInfoList()
							.stream()
							.map(myLicenseInfoResponse -> myLicenseInfoResponse.getProductName())
							.toArray(String[]::new);
						memberInfoDTO.setLicenseProducts(licenseProducts);
					}
					memberInfoDTO.setLicenseProducts(licenseProducts);
					memberInfoDTOList.add(memberInfoDTO);
				}

			}
			pageMetadataResponse.setTotalElements(workspaceUserPage.getTotalElements());
			pageMetadataResponse.setTotalPage(workspaceUserPage.getTotalPages());
			pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
			pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

			List<MemberInfoDTO> resultMemberListResponse = getSortedMemberList(pageRequest, memberInfoDTOList);
			return new ApiResponse<>(new MemberListResponse(resultMemberListResponse, pageMetadataResponse));
		}
		//아무것도 filter 건 게 없는 경우
		if (workspaceRoleList.isEmpty() && licenseProductList.isEmpty()) {
			List<String> userIdList = userInfoListRestResponse.getUserInfoList()
				.stream()
				.map(userInfoRestResponse -> userInfoRestResponse.getUuid())
				.collect(Collectors.toList());

			Page<WorkspaceUser> workspaceUserPage = this.workspaceUserRepository.findByWorkspace_UuidAndUserIdIn(
				workspaceId, userIdList, newPageable);
			List<WorkspaceUser> resultWorkspaceUser = workspaceUserPage.toList();

			for (UserInfoRestResponse userInfoRestResponse : userInfoListRestResponse.getUserInfoList()) {

				WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
					workspace, userInfoRestResponse.getUuid());
				if (resultWorkspaceUser.contains(workspaceUserPermission.getWorkspaceUser())) {
					MemberInfoDTO memberInfoDTO = this.modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
					memberInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
					memberInfoDTO.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());
					memberInfoDTO.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());

					String[] licenseProducts = new String[0];
					MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
						workspaceId, userInfoRestResponse.getUuid()).getData();
					if (myLicenseInfoListResponse.getLicenseInfoList() != null
						&& !myLicenseInfoListResponse.getLicenseInfoList().isEmpty()) {
						licenseProducts = myLicenseInfoListResponse.getLicenseInfoList()
							.stream()
							.map(myLicenseInfoResponse -> myLicenseInfoResponse.getProductName())
							.toArray(String[]::new);
						memberInfoDTO.setLicenseProducts(licenseProducts);
					}
					memberInfoDTO.setLicenseProducts(licenseProducts);
					memberInfoDTOList.add(memberInfoDTO);
				}
			}
			pageMetadataResponse.setTotalElements(workspaceUserPage.getTotalElements());
			pageMetadataResponse.setTotalPage(workspaceUserPage.getTotalPages());
			pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
			pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

			List<MemberInfoDTO> resultMemberListResponse = getSortedMemberList(pageRequest, memberInfoDTOList);
			return new ApiResponse<>(new MemberListResponse(resultMemberListResponse, pageMetadataResponse));
		}

		List<MemberInfoDTO> resultMemberListResponse = getSortedMemberList(pageRequest, memberInfoDTOList);
		return new ApiResponse<>(new MemberListResponse(resultMemberListResponse, pageMetadataResponse));
	}

	public List<MemberInfoDTO> getSortedMemberList(
		com.virnect.workspace.global.common.PageRequest pageRequest, List<MemberInfoDTO> memberInfoDTOList
	) {

		String sortName = pageRequest.of().getSort().toString().split(":")[0].trim();//sort의 기준이 될 열
		String sortDirection = pageRequest.of().getSort().toString().split(":")[1].trim();//sort의 방향 : 내림차순 or 오름차순

		if (sortName.equalsIgnoreCase("role") && sortDirection.equalsIgnoreCase("asc")) {
			return memberInfoDTOList.stream()
				.sorted(
					Comparator.comparing(MemberInfoDTO::getRoleId, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("role") && sortDirection.equalsIgnoreCase("desc")) {
			return memberInfoDTOList.stream()
				.sorted(
					Comparator.comparing(MemberInfoDTO::getRoleId, Comparator.nullsFirst(Comparator.reverseOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("email") && sortDirection.equalsIgnoreCase("asc")) {
			return memberInfoDTOList.stream()
				.sorted(Comparator.comparing(MemberInfoDTO::getEmail, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("email") && sortDirection.equalsIgnoreCase("desc")) {
			return memberInfoDTOList.stream()
				.sorted(Comparator.comparing(MemberInfoDTO::getEmail, Comparator.nullsFirst(Comparator.reverseOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("joinDate") && sortDirection.equalsIgnoreCase("asc")) {
			return memberInfoDTOList.stream()
				.sorted(
					Comparator.comparing(MemberInfoDTO::getJoinDate, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("joinDate") && sortDirection.equalsIgnoreCase("desc")) {
			return memberInfoDTOList.stream()
				.sorted(
					Comparator.comparing(MemberInfoDTO::getJoinDate, Comparator.nullsFirst(Comparator.reverseOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("nickname") && sortDirection.equalsIgnoreCase("asc")) {
			return memberInfoDTOList.stream()
				.sorted(
					Comparator.comparing(MemberInfoDTO::getNickName, Comparator.nullsFirst(Comparator.naturalOrder())))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("nickname") && sortDirection.equalsIgnoreCase("desc")) {
			return memberInfoDTOList.stream()
				.sorted(
					Comparator.comparing(MemberInfoDTO::getNickName, Comparator.nullsFirst(Comparator.reverseOrder())))
				.collect(Collectors.toList());
		} else {
			return memberInfoDTOList.stream()
				.sorted(Comparator.comparing(
					MemberInfoDTO::getUpdatedDate,
					Comparator.nullsFirst(Comparator.reverseOrder())
				))
				.collect(Collectors.toList());

		}
	}

	/**
	 * 워크스페이스 정보 조회
	 *
	 * @param workspaceId - 워크스페이스 uuid
	 * @param userId      - 사용자 uuid
	 * @return - 워크스페이스 정보
	 */
	public ApiResponse<WorkspaceInfoResponse> getWorkspaceDetailInfo(String workspaceId) {
		//workspace 정보 set
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceInfoDTO workspaceInfo = modelMapper.map(workspace, WorkspaceInfoDTO.class);
		workspaceInfo.setMasterUserId(workspace.getUserId());

		//user 정보 set
		List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId);
		List<UserInfoDTO> userInfoList = new ArrayList<>();
		workspaceUserList.stream().forEach(workspaceUser -> {
			UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(
				workspaceUser.getUserId()).getData();
			UserInfoDTO userInfoDTO = modelMapper.map(userInfoRestResponse, UserInfoDTO.class);
			userInfoList.add(userInfoDTO);
			WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser(
				workspaceUser);
			userInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
		});

		//role 정보 set
		long masterUserCount = this.workspaceUserPermissionRepository.countByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(
			workspace, "MASTER");
		long managerUserCount = this.workspaceUserPermissionRepository.countByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(
			workspace, "MANAGER");
		long memberUserCount = this.workspaceUserPermissionRepository.countByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(
			workspace, "MEMBER");

		//plan 정보 set
		int remotePlanCount = 0;
		int makePlanCount = 0;
		int viewPlanCount = 0;

		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() != null
			&& !workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			for (WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse licenseProductInfoResponse : workspaceLicensePlanInfoResponse
				.getLicenseProductInfoList()) {
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
		return new ApiResponse<>(
			new WorkspaceInfoResponse(workspaceInfo, userInfoList, masterUserCount, managerUserCount, memberUserCount,
				remotePlanCount, makePlanCount, viewPlanCount
			));
	}

	public ApiResponse<WorkspaceInfoDTO> getWorkspaceInfo(String workspaceId) {
		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceInfoDTO workspaceInfoDTO = modelMapper.map(workspace, WorkspaceInfoDTO.class);
		workspaceInfoDTO.setMasterUserId(workspace.getUserId());
		return new ApiResponse<>(workspaceInfoDTO);
	}

	/**
	 * 유저 정보 조회(User Service)
	 *
	 * @param userId - 유저 uuid
	 * @return - 유저 정보
	 */
	private UserInfoRestResponse getUserInfo(String userId) {
		ApiResponse<UserInfoRestResponse> userInfoResponse = this.userRestService.getUserInfoByUserId(userId);
		return userInfoResponse.getData();
	}

	/**
	 * 워크스페이스 유저 초대
	 *
	 * @param workspaceId            - 초대 할 워크스페이스 uuid
	 * @param workspaceInviteRequest - 초대 유저 정보
	 * @return
	 */
	public ApiResponse<Boolean> inviteWorkspace(
		String workspaceId, WorkspaceInviteRequest workspaceInviteRequest, Locale locale
	) {
		// 워크스페이스 플랜 조회하여 최대 초대 가능 명 수를 초과했는지 체크
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null
			|| workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
		}
		int workspaceUserAmount = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId).size();
		if (workspaceLicensePlanInfoResponse.getMaxUserAmount()
			< workspaceUserAmount + workspaceInviteRequest.getUserInfoList().size()) {
			throw new WorkspaceException(ErrorCode.ERR_NOMORE_JOIN_WORKSPACE);
		}

		//초대요청 라이선스
		int requestRemote = 0, requestMake = 0, requestView = 0;
		for (WorkspaceInviteRequest.UserInfo userInfo : workspaceInviteRequest.getUserInfoList()) {
			//초대받은 사람의 유저의 권한은 매니저 또는 멤버만 가능하도록 체크x
			if (userInfo.getRole().equals("MASTER")) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
			//초대받는 사람에게 부여되는 라이선스는 최소 1개 이상이도록 체크
			userLicenseValidCheck(userInfo.getPlanRemote(), userInfo.getPlanMake(), userInfo.getPlanView());

			if (userInfo.getPlanRemote()) {
				requestRemote++;
			}
			if (userInfo.getPlanMake()) {
				requestMake++;
			}
			if (userInfo.getPlanView()) {
				requestView++;
			}
		}

		//초대받는 사람에게 할당할 라이선스가 있는 지 체크.(useful license check)
		for (WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse licenseProductInfo : workspaceLicensePlanInfoResponse
			.getLicenseProductInfoList()) {

			if (licenseProductInfo.getProductName().equals(LicenseProduct.REMOTE.toString())) {
				log.debug(
					"[WORKSPACE INVITE USER] Workspace Useful License Check. Workspace Unuse Remote License count >> {}, Request REMOTE License count >> {}",
					licenseProductInfo.getUnUseLicenseAmount(),
					requestRemote
				);
				if (!licenseProductInfo.getProductStatus().equals(LicenseProductStatus.ACTIVE)) {
					log.error(
						"[WORKSPACE INVITE USER] REMOTE License Product Status is not active. Product Status >>[{}]",
						licenseProductInfo.getProductStatus()
					);
					throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
				}
				if (licenseProductInfo.getUnUseLicenseAmount() < requestRemote) {
					throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
				}
			}
			if (licenseProductInfo.getProductName().equals(LicenseProduct.MAKE.toString())) {
				log.debug(
					"[WORKSPACE INVITE USER] Workspace Useful License Check. Workspace Unuse Make License count >> {}, Request MAKE License count >> {}",
					licenseProductInfo.getUnUseLicenseAmount(),
					requestMake
				);
				if (!licenseProductInfo.getProductStatus().equals(LicenseProductStatus.ACTIVE)) {
					log.error(
						"[WORKSPACE INVITE USER] MAKE License Product Status is not active. Product Status >>[{}]",
						licenseProductInfo.getProductStatus()
					);
					throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
				}
				if (licenseProductInfo.getUnUseLicenseAmount() < requestMake) {
					throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
				}
			}
			if (licenseProductInfo.getProductName().equals(LicenseProduct.VIEW.toString())) {
				log.debug(
					"[WORKSPACE INVITE USER] Workspace Useful License Check. Workspace Unuse View License count >> {}, Request VIEW License count >> {}",
					licenseProductInfo.getUnUseLicenseAmount(),
					requestView
				);
				if (!licenseProductInfo.getProductStatus().equals(LicenseProductStatus.ACTIVE)) {
					log.error(
						"[WORKSPACE INVITE USER] VIEW License Product Status is not active. Product Status >>[{}]",
						licenseProductInfo.getProductStatus()
					);
					throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
				}
				if (licenseProductInfo.getUnUseLicenseAmount() < requestView) {
					throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
				}
			}
		}

		//라이선스 플랜 타입 구하기 -- basic, pro..(한 워크스페이스에서 다른 타입의 라이선스 플랜을 동시에 가지고 있을 수 없으므로, 아무 플랜이나 잡고 타입을 구함.)
		String licensePlanType = workspaceLicensePlanInfoResponse.getLicenseProductInfoList()
			.stream()
			.map(licenseProductInfoResponse -> licenseProductInfoResponse.getLicenseType())
			.collect(Collectors.toList())
			.get(0);

		// 요청한 사람이 마스터유저 또는 매니저유저인지 체크
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, workspaceInviteRequest.getUserId());
		if (workspaceUserPermission.getWorkspaceRole().getRole().equals("MEMBER")) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		// 초대할 유저의 계정 유효성 체크(user 서비스)
		List<String> emailList = new ArrayList<>();
		workspaceInviteRequest.getUserInfoList().stream().forEach(userInfo -> emailList.add(userInfo.getEmail()));
		InviteUserInfoRestResponse responseUserList = this.userRestService.getUserInfoByEmailList(
			emailList.stream().toArray(String[]::new)).getData();

		// 유효하지 않은 이메일을 가진 사용자가 포함되어 있는 경우.
		if (emailList.size() != responseUserList.getInviteUserInfoList().size()) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_USER_EXIST);
		}
		//TODO : 서브유저로 등록되어 있는 사용자가 포함되어 있는 경우.

		//마스터 유저 정보
		UserInfoRestResponse materUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();

		Long duration = Duration.ofDays(7).getSeconds();
		responseUserList.getInviteUserInfoList().stream().forEach(inviteUserResponse -> {
			//이미 이 워크스페이스에 소속되어 있는 경우
			if (this.workspaceUserRepository.findByUserIdAndWorkspace(inviteUserResponse.getUserUUID(), workspace)
				!= null) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ALREADY_EXIST);
			}
			workspaceInviteRequest.getUserInfoList().stream().forEach(userInfo -> {
				if (inviteUserResponse.getEmail().equals(userInfo.getEmail())) {
					//redis 긁어서 이미 초대한 정보 있는지 확인하고, 있으면 시간과 초대 정보 업데이트
					UserInvite userInvite = this.userInviteRepository.findById(
						inviteUserResponse.getUserUUID() + "-" + workspaceId).orElse(null);
					if (userInvite != null) {
						userInvite.setRole(userInfo.getRole());
						userInvite.setPlanRemote(userInfo.getPlanRemote());
						userInvite.setPlanMake(userInfo.getPlanMake());
						userInvite.setPlanView(userInfo.getPlanView());
						userInvite.setUpdatedDate(LocalDateTime.now());
						userInvite.setExpireTime(duration);
						this.userInviteRepository.save(userInvite);
						log.debug(
							"[WORKSPACE INVITE USER] Worksapce Invite Info Redis Update >> {}", userInvite.toString());
					} else {
						UserInvite newUserInvite = UserInvite.builder()
							.inviteId(inviteUserResponse.getUserUUID() + "-" + workspaceId)
							.responseUserId(inviteUserResponse.getUserUUID())
							.responseUserEmail(inviteUserResponse.getEmail())
							.responseUserName(inviteUserResponse.getName())
							.responseUserNickName(inviteUserResponse.getNickname())
							.requestUserId(materUser.getUuid())
							.requestUserEmail(materUser.getEmail())
							.requestUserName(materUser.getName())
							.requestUserNickName(materUser.getNickname())
							.workspaceId(workspace.getUuid())
							.workspaceName(workspace.getName())
							.role(userInfo.getRole())
							.planRemote(userInfo.getPlanRemote())
							.planMake(userInfo.getPlanMake())
							.planView(userInfo.getPlanView())
							.planRemoteType(licensePlanType)
							.planMakeType(licensePlanType)
							.planViewType(licensePlanType)
							.invitedDate(LocalDateTime.now())
							.updatedDate(null)
							.expireTime(duration)
							.build();
						this.userInviteRepository.save(newUserInvite);
						log.debug(
							"[WORKSPACE INVITE USER] Worksapce Invite Info Redis Set >> {}", newUserInvite.toString());
					}
					//메일은 이미 초대한 것 여부와 관계없이 발송한다.
					String rejectUrl = serverUrl + "/workspaces/" + workspaceId + "/invite/accept?userId="
						+ inviteUserResponse.getUserUUID() + "&accept=false&lang=" + locale.getLanguage();
					String acceptUrl = serverUrl + "/workspaces/" + workspaceId + "/invite/accept?userId="
						+ inviteUserResponse.getUserUUID() + "&accept=true&lang=" + locale.getLanguage();
					Context context = new Context();
					context.setVariable("workspaceMasterNickName", materUser.getNickname());
					context.setVariable("workspaceMasterEmail", materUser.getEmail());
					context.setVariable("workspaceName", workspace.getName());
					context.setVariable("workstationHomeUrl", redirectUrl);
					context.setVariable("rejectUrl", rejectUrl);
					context.setVariable("acceptUrl", acceptUrl);
					context.setVariable("responseUserName", inviteUserResponse.getName());
					context.setVariable("responseUserEmail", inviteUserResponse.getEmail());
					context.setVariable("responseUserNickName", inviteUserResponse.getNickname());
					context.setVariable("role", userInfo.getRole());
					context.setVariable(
						"plan",
						generatePlanString(userInfo.getPlanRemote(), userInfo.getPlanMake(), userInfo.getPlanView())
					);
					context.setVariable("supportUrl", supportUrl);
					String subject = this.messageSource.getMessage(Mail.WORKSPACE_INVITE.getSubject(), null, locale);
					String template = this.messageSource.getMessage(Mail.WORKSPACE_INVITE.getTemplate(), null, locale);
					String html = springTemplateEngine.process(template, context);

					List<String> emailReceiverList = new ArrayList<>();
					emailReceiverList.add(inviteUserResponse.getEmail());

					this.sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);
				}
			});
		});

		return new ApiResponse<>(true);
	}

	/**
	 * message 서비스로 메일 발송 요청
	 *
	 * @param html
	 * @param receivers
	 * @param mailSender
	 * @param mailSubject
	 */
	private void sendMailRequest(String html, List<String> receivers, String sender, String subject) {
		MailRequest mailRequest = new MailRequest();
		mailRequest.setHtml(html);
		mailRequest.setReceivers(receivers);
		mailRequest.setSender(sender);
		mailRequest.setSubject(subject);
		this.messageRestService.sendMail(mailRequest);
	}

	public RedirectView inviteWorkspaceResult(String workspaceId, String userId, Boolean accept, String lang) {
		Locale locale = new Locale(lang, "");
		if (accept) {
			return this.inviteWorkspaceAccept(workspaceId, userId, locale);
		} else {
			return this.inviteWorkspaceReject(workspaceId, userId, locale);
		}
	}

	public RedirectView inviteWorkspaceAccept(String workspaceId, String userId, Locale locale) {
		UserInvite userInvite = this.userInviteRepository.findById(userId + "-" + workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_NOT_FOUND_INVITE_WORKSPACE_INFO));
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

		//메일 발송 수신자 : 마스터 유저, 매니저 유저
		List<String> emailReceiverList = new ArrayList<>();
		UserInfoRestResponse masterUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
		emailReceiverList.add(masterUser.getEmail());

		List<WorkspaceUserPermission> workspaceUserPermissionList = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(
			workspace, "MANAGER");
		if (workspaceUserPermissionList != null) {
			workspaceUserPermissionList.stream().forEach(workspaceUserPermission -> {
				UserInfoRestResponse managerUser = this.userRestService.getUserInfoByUserId(workspace.getUserId())
					.getData();
				emailReceiverList.add(managerUser.getEmail());
			});
		}

		//이미 마스터, 매니저, 멤버로 소속되어 있는 워크스페이스 최대 개수 9개 체크 <-- 제한 수 없어짐
        /*if (this.workspaceUserRepository.countWorkspaceUsersByUserId(userId) > 8) {
            Context context = new Context();
            context.setVariable("workspaceName", workspace.getName());
            context.setVariable("workspaceMasterNickName", masterUser.getNickname());
            context.setVariable("workspaceMasterEmail", masterUser.getEmail());
            context.setVariable("userNickName", userInvite.getResponseUserNickName());
            context.setVariable("userEmail", userInvite.getResponseUserEmail());
            context.setVariable("plan", generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView()));
            context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
            context.setVariable("planMakeType", userInvite.getPlanMakeType());
            context.setVariable("planViewType", userInvite.getPlanViewType());
            context.setVariable("workstationHomeUrl", redirectUrl);
            context.setVariable("workstationMembersUrl", redirectUrl + "/members");
            context.setVariable("supportUrl", supportUrl);

            String subject = this.messageSource.getMessage(Mail.WORKSPACE_OVER_JOIN_FAIL.getSubject(), null, locale);
            String template = this.messageSource.getMessage(Mail.WORKSPACE_OVER_JOIN_FAIL.getTemplate(), null, locale);
            String html = springTemplateEngine.process(template, context);

            this.sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

            this.userInviteRepository.deleteById(userId + "-" + workspaceId);

            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(redirectUrl + RedirectPath.WORKSPACE_OVER_JOIN_FAIL.getValue());
            redirectView.setContentType("application/json");
            return redirectView;
        }*/

		//라이선스 플랜 - 라이선스 플랜 보유 체크, 멤버 제한 수 체크
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null
			|| workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
		}

		int workspaceUserAmount = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId).size();

		if (workspaceLicensePlanInfoResponse.getMaxUserAmount() < workspaceUserAmount + 1) {
			Context context = new Context();
			context.setVariable("workspaceName", workspace.getName());
			context.setVariable("workspaceMasterNickName", masterUser.getNickname());
			context.setVariable("workspaceMasterEmail", masterUser.getEmail());
			context.setVariable("userNickName", userInvite.getResponseUserNickName());
			context.setVariable("userEmail", userInvite.getResponseUserEmail());
			context.setVariable(
				"plan",
				generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView())
			);
			context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
			context.setVariable("planMakeType", userInvite.getPlanMakeType());
			context.setVariable("planViewType", userInvite.getPlanViewType());
			context.setVariable("contactUrl", contactUrl);
			context.setVariable("workstationHomeUrl", redirectUrl);
			context.setVariable("supportUrl", supportUrl);

			String subject = this.messageSource.getMessage(
				Mail.WORKSPACE_OVER_MAX_USER_FAIL.getSubject(), null, locale);
			String template = this.messageSource.getMessage(
				Mail.WORKSPACE_OVER_MAX_USER_FAIL.getTemplate(), null, locale);
			String html = springTemplateEngine.process(template, context);
			this.sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

			this.userInviteRepository.deleteById(userId + "-" + workspaceId);
			RedirectView redirectView = new RedirectView();
			redirectView.setUrl(redirectUrl + RedirectPath.WORKSPACE_OVER_MAX_USER_FAIL.getValue());
			redirectView.setContentType("application/json");
			return redirectView;
		}
		//플랜 할당.
		Boolean planRemoteGrantResult = true;
		Boolean planMakeGrantResult = true;
		Boolean planViewGrantResult = true;
		StringBuilder successPlan = new StringBuilder();
		StringBuilder failPlan = new StringBuilder();

		if (userInvite.getPlanRemote()) {
			MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(
				workspaceId, userId, LicenseProduct.REMOTE.toString()).getData();
			if (!grantResult.getProductName().equals(LicenseProduct.REMOTE.toString())) {
				planRemoteGrantResult = false;
				failPlan.append("REMOTE");
			} else {
				successPlan.append("REMOTE");
			}
		}
		if (userInvite.getPlanMake()) {
			MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(
				workspaceId, userId, LicenseProduct.MAKE.toString()).getData();
			if (!grantResult.getProductName().equals(LicenseProduct.MAKE.toString())) {
				planMakeGrantResult = false;
				failPlan.append(",MAKE");
			} else {
				successPlan.append(",MAKE");
			}
		}
		if (userInvite.getPlanView()) {
			MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(
				workspaceId, userId, LicenseProduct.VIEW.toString()).getData();
			if (!grantResult.getProductName().equals(LicenseProduct.VIEW.toString())) {
				planViewGrantResult = false;
				failPlan.append(",VIEW");
			} else {
				successPlan.append(",VIEW");
			}
		}

		if (!planRemoteGrantResult || !planMakeGrantResult || !planViewGrantResult) {
			Context context = new Context();
			context.setVariable("workspaceName", workspace.getName());
			context.setVariable("workspaceMasterNickName", masterUser.getNickname());
			context.setVariable("workspaceMasterEmail", masterUser.getEmail());
			context.setVariable("userNickName", userInvite.getResponseUserNickName());
			context.setVariable("userEmail", userInvite.getResponseUserEmail());
			context.setVariable("successPlan", successPlan);
			context.setVariable("failPlan", failPlan);
			context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
			context.setVariable("planMakeType", userInvite.getPlanMakeType());
			context.setVariable("planViewType", userInvite.getPlanViewType());
			context.setVariable("workstationHomeUrl", redirectUrl);
			context.setVariable("workstationMembersUrl", redirectUrl + "/members");
			context.setVariable("supportUrl", supportUrl);

			String subject = this.messageSource.getMessage(Mail.WORKSPACE_OVER_PLAN_FAIL.getSubject(), null, locale);
			String template = this.messageSource.getMessage(Mail.WORKSPACE_OVER_PLAN_FAIL.getTemplate(), null, locale);
			String html = springTemplateEngine.process(template, context);
			this.sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

			this.userInviteRepository.deleteById(userId + "-" + workspaceId);

			RedirectView redirectView = new RedirectView();
			redirectView.setUrl(redirectUrl + RedirectPath.WORKSPACE_OVER_PLAN_FAIL.getValue());
			redirectView.setContentType("application/json");
			return redirectView;

		}
		//워크스페이스 소속 넣기 (workspace_user)
		WorkspaceUser workspaceUser = setWorkspaceUserInfo(workspaceId, userId);
		this.workspaceUserRepository.save(workspaceUser);

		//워크스페이스 권한 부여하기 (workspace_user_permission)
		WorkspaceRole workspaceRole = this.workspaceRoleRepository.findByRole(userInvite.getRole().toUpperCase());
		WorkspacePermission workspacePermission = WorkspacePermission.builder().id(Permission.ALL.getValue()).build();
		WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
			.workspaceUser(workspaceUser)
			.workspaceRole(workspaceRole)
			.workspacePermission(workspacePermission)
			.build();
		this.workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

		//MAIL 발송
		Context context = new Context();
		context.setVariable("workspaceName", workspace.getName());
		context.setVariable("workspaceMasterNickName", masterUser.getNickname());
		context.setVariable("workspaceMasterEmail", masterUser.getEmail());
		context.setVariable("acceptUserNickName", userInvite.getResponseUserNickName());
		context.setVariable("acceptUserEmail", userInvite.getResponseUserEmail());
		context.setVariable("role", userInvite.getRole());
		context.setVariable("workstationHomeUrl", redirectUrl);
		context.setVariable(
			"plan", generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView()));
		context.setVariable("supportUrl", supportUrl);

		String subject = this.messageSource.getMessage(Mail.WORKSPACE_INVITE_ACCEPT.getSubject(), null, locale);
		String template = this.messageSource.getMessage(Mail.WORKSPACE_INVITE_ACCEPT.getTemplate(), null, locale);
		String html = springTemplateEngine.process(template, context);
		this.sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

		//redis 에서 삭제
		this.userInviteRepository.deleteById(userId + "-" + workspaceId);

		//history 저장
		String message;
		if (workspaceRole.getRole().equalsIgnoreCase("MANAGER")) {
			message = this.messageSource.getMessage(
				"WORKSPACE_INVITE_MANAGER", new String[] {userInvite.getResponseUserNickName(),
					generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView())},
				locale
			);
		} else {
			message = this.messageSource.getMessage(
				"WORKSPACE_INVITE_MEMBER", new String[] {userInvite.getResponseUserNickName(),
					generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView())},
				locale
			);
		}
		History history = History.builder()
			.message(message)
			.userId(userInvite.getResponseUserId())
			.workspace(workspace)
			.build();
		this.historyRepository.save(history);

		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(redirectUrl);
		redirectView.setContentType("application/json");
		return redirectView;

	}

	private String generatePlanString(Boolean remote, Boolean make, Boolean view) {
		StringBuilder plan = new StringBuilder();
		if (remote) {
			plan.append("REMOTE");
		}
		if (make) {
			plan.append(",MAKE");
		}
		if (view) {
			plan.append(",VIEW");
		}
		return plan.toString();
	}

	public RedirectView inviteWorkspaceReject(String workspaceId, String userId, Locale locale) {
		//REDIS 에서 초대정보 조회
		UserInvite userInvite = this.userInviteRepository.findById(userId + "-" + workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_NOT_FOUND_INVITE_WORKSPACE_INFO));
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

		//워크스페이스 초대 거절 메일 수신자 : 마스터, 매니저
		List<String> emailReceiverList = new ArrayList<>();
		UserInfoRestResponse masterUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
		emailReceiverList.add(masterUser.getEmail());

		List<WorkspaceUserPermission> workspaceUserPermissionList = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(
			workspace, "MANAGER");
		if (workspaceUserPermissionList != null) {
			workspaceUserPermissionList.stream().forEach(workspaceUserPermission -> {
				UserInfoRestResponse managerUser = this.userRestService.getUserInfoByUserId(workspace.getUserId())
					.getData();
				emailReceiverList.add(managerUser.getEmail());
			});
		}

		//redis에서 삭제
		this.userInviteRepository.deleteById(userId + "-" + workspaceId);

		//MAIL 발송
		Context context = new Context();
		context.setVariable("rejectUserNickname", userInvite.getResponseUserNickName());
		context.setVariable("rejectUserEmail", userInvite.getResponseUserEmail());
		context.setVariable("workspaceName", workspace.getName());
		context.setVariable("accountUrl", accountUrl);
		context.setVariable("supportUrl", supportUrl);

		String subject = this.messageSource.getMessage(Mail.WORKSPACE_INVITE_REJECT.getSubject(), null, locale);
		String template = this.messageSource.getMessage(Mail.WORKSPACE_INVITE_REJECT.getTemplate(), null, locale);
		String html = springTemplateEngine.process(template, context);
		this.sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(redirectUrl);
		redirectView.setContentType("application/json");
		return redirectView;
	}

	/**
	 * 권한 변경 기능
	 * 권한 변경에는 워크스페이스 내의 유저 권한 변경, 플랜 변경이 있음.
	 * 유저 권한 변경은 해당 워크스페이스의 '마스터'유저만 가능함.('매니저' to '멤버', '멤버' to '매니저')
	 * 플랜 변경은 해당 워크스페이스 '마스터', '매니저'유저 가 가능함.
	 * 이때 주의점은 어느 유저든지 간에 최소 1개 이상의 제품라이선스를 보유하고 있어야 함.
	 *
	 * @param workspaceId         - 권한 변경이 이루어지는 워크스페이스의 식별자
	 * @param memberUpdateRequest - 권한 변경 요청 정보
	 * @param locale              - 언어 정보
	 * @return - 변경 성공 여부
	 */
	public ApiResponse<Boolean> reviseMemberInfo(
		String workspaceId, MemberUpdateRequest memberUpdateRequest, Locale locale
	) {

		Workspace workspace = workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUserPermission userPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, memberUpdateRequest.getUserId());

		WorkspaceRole workspaceRole = this.workspaceRoleRepository.findByRole(
			memberUpdateRequest.getRole().toUpperCase());
		UserInfoRestResponse masterUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
		UserInfoRestResponse user = this.userRestService.getUserInfoByUserId(memberUpdateRequest.getUserId()).getData();
		UserInfoRestResponse requestUser = this.userRestService.getUserInfoByUserId(
			memberUpdateRequest.getRequestUserId()).getData();

		//권한 변경
		if (!userPermission.getWorkspaceRole().equals(workspaceRole)) {
			updateUserPermission(
				workspace, memberUpdateRequest.getRequestUserId(), memberUpdateRequest.getUserId(), workspaceRole,
				masterUser, user, locale
			);
		}

		//플랜 변경
		workspaceUserLicenseHandling(
			memberUpdateRequest.getUserId(), workspace, masterUser, user, requestUser,
			memberUpdateRequest.getLicenseRemote(), memberUpdateRequest.getLicenseMake(),
			memberUpdateRequest.getLicenseView(), locale
		);

		return new ApiResponse<>(true);
	}

	private void workspaceUserLicenseHandling(
		String userId, Workspace workspace, UserInfoRestResponse masterUser, UserInfoRestResponse user,
		UserInfoRestResponse requestUser, Boolean remoteLicense, Boolean makeLicense, Boolean viewLicense, Locale locale
	) {
		userLicenseValidCheck(remoteLicense, makeLicense, viewLicense);

		//사용자의 예전 라이선스정보 가져오기
		MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
			workspace.getUuid(), userId).getData();
		List<String> oldProductList = new ArrayList<>();
		if (myLicenseInfoListResponse.getLicenseInfoList() != null && !myLicenseInfoListResponse.getLicenseInfoList()
			.isEmpty()) {
			oldProductList = myLicenseInfoListResponse.getLicenseInfoList()
				.stream()
				.map(myLicenseInfoResponse -> myLicenseInfoResponse.getProductName())
				.collect(Collectors.toList());
		}
		List<LicenseProduct> notFoundProductList = new ArrayList<>();

		List<LicenseProduct> newProductList = new ArrayList<>();
		if (remoteLicense) {
			if (!oldProductList.contains(LicenseProduct.REMOTE.toString())) {
				//CASE1 : 기존에 없던 라이선스인데 사용하는 경우면
				newProductList.add(LicenseProduct.REMOTE);
				MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(
					workspace.getUuid(), userId, LicenseProduct.REMOTE.toString()).getData();
				if (!grantResult.getProductName().equals(LicenseProduct.REMOTE.toString())) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
				}
			}
			//CASE2 : 기존에 있던 라이선스인데 사용하는 경우면
		} else {
			//CASE3 : 기존에 있던 라이선스인데 사용안하는 경우면
			if (oldProductList.contains(LicenseProduct.REMOTE.toString())) {
				notFoundProductList.add(LicenseProduct.REMOTE);
				Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(
					workspace.getUuid(), userId, LicenseProduct.REMOTE.toString()).getData();
				if (!revokeResult) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
				}
			}
			//CASE4 : 기존에 없던 라이선스인데 사용안하는 경우면
		}

		if (makeLicense) {
			if (!oldProductList.contains(LicenseProduct.MAKE.toString())) {
				newProductList.add(LicenseProduct.MAKE);
				MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(
					workspace.getUuid(), userId, LicenseProduct.MAKE.toString()).getData();
				if (!grantResult.getProductName().equals(LicenseProduct.MAKE.toString())) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
				}
			}
		} else {
			if (oldProductList.contains(LicenseProduct.MAKE.toString())) {
				notFoundProductList.add(LicenseProduct.MAKE);
				Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(
					workspace.getUuid(), userId, LicenseProduct.MAKE.toString()).getData();
				if (!revokeResult) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
				}
			}
		}

		if (viewLicense) {
			if (!oldProductList.contains(LicenseProduct.VIEW.toString())) {
				newProductList.add(LicenseProduct.VIEW);
				MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(
					workspace.getUuid(), userId, LicenseProduct.VIEW.toString()).getData();
				if (!grantResult.getProductName().equals(LicenseProduct.VIEW.toString())) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
				}
			}
		} else {
			if (oldProductList.contains(LicenseProduct.VIEW.toString())) {
				notFoundProductList.add(LicenseProduct.VIEW);
				Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(
					workspace.getUuid(), userId, LicenseProduct.VIEW.toString()).getData();
				if (!revokeResult) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
				}
			}
		}

		if (!newProductList.isEmpty() || !notFoundProductList.isEmpty()) {
			//히스토리
			if (!newProductList.isEmpty()) {
				String newProductString = newProductList.stream()
					.map(licenseProduct -> String.valueOf(licenseProduct))
					.collect(Collectors.joining());
				String message = this.messageSource.getMessage(
					"WORKSPACE_GRANT_LICENSE",
					new java.lang.String[] {requestUser.getNickname(), user.getNickname(), newProductString}, locale
				);
				saveHistotry(workspace, userId, message);
			}
			if (!notFoundProductList.isEmpty()) {
				String notFoundProductString = notFoundProductList.stream()
					.map(licenseProduct -> String.valueOf(licenseProduct))
					.collect(Collectors.joining());
				String message = this.messageSource.getMessage(
					"WORKSPACE_REVOKE_LICENSE",
					new java.lang.String[] {requestUser.getNickname(), user.getNickname(), notFoundProductString},
					locale
				);
				saveHistotry(workspace, userId, message);
			}

			//메일 발송
			Context context = new Context();
			context.setVariable("workspaceName", workspace.getName());
			context.setVariable("workstationHomeUrl", redirectUrl);
			context.setVariable("workspaceMasterNickName", masterUser.getNickname());
			context.setVariable("workspaceMasterEmail", masterUser.getEmail());
			context.setVariable("responseUserNickName", user.getNickname());
			context.setVariable("responseUserEmail", user.getEmail());
			context.setVariable("supportUrl", supportUrl);

			StringBuilder plan = new StringBuilder();
			if (remoteLicense) {
				plan.append("REMOTE");
			}
			if (makeLicense) {
				plan.append(",MAKE");
			}
			if (viewLicense) {
				plan.append(",VIEW");
			}
			context.setVariable("plan", plan.toString());

			List<String> receiverEmailList = new ArrayList<>();
			receiverEmailList.add(user.getEmail());
			String subject = this.messageSource.getMessage(Mail.WORKSPACE_USER_PLAN_UPDATE.getSubject(), null, locale);
			String template = this.messageSource.getMessage(
				Mail.WORKSPACE_USER_PLAN_UPDATE.getTemplate(), null, locale);
			String html = springTemplateEngine.process(template, context);
			this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);
		}

	}

	private void updateUserPermission(
		Workspace workspace, String requestUserId, String responseUserId, WorkspaceRole workspaceRole,
		UserInfoRestResponse masterUser, UserInfoRestResponse user, Locale locale
	) {
		//1. 요청자 권한 확인(마스터만 가능)
		WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, requestUserId);
		String role = workspaceUserPermission.getWorkspaceRole().getRole();
		if (role == null || !role.equalsIgnoreCase("MASTER")) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//2. 대상자 권한 확인(매니저, 멤버 권한만 가능)
		WorkspaceUserPermission userPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, responseUserId);
		String userRole = userPermission.getWorkspaceRole().getRole();
		if (userRole == null || userRole.equalsIgnoreCase("MASTER")) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//3. 권한 변경
		userPermission.setWorkspaceRole(workspaceRole);
		this.workspaceUserPermissionRepository.save(userPermission);

		// 메일 발송
		Context context = new Context();
		context.setVariable("workspaceName", workspace.getName());
		context.setVariable("workspaceMasterNickName", masterUser.getNickname());
		context.setVariable("workspaceMasterEmail", masterUser.getEmail());
		context.setVariable("responseUserNickName", user.getNickname());
		context.setVariable("responseUserEmail", user.getEmail());
		context.setVariable("role", workspaceRole.getRole());
		context.setVariable("workstationHomeUrl", redirectUrl);
		context.setVariable("supportUrl", supportUrl);

		List<String> receiverEmailList = new ArrayList<>();
		receiverEmailList.add(user.getEmail());
		String subject = this.messageSource.getMessage(
			Mail.WORKSPACE_USER_PERMISSION_UPDATE.getSubject(), null, locale);
		String template = this.messageSource.getMessage(
			Mail.WORKSPACE_USER_PERMISSION_UPDATE.getTemplate(), null, locale);
		String html = springTemplateEngine.process(template, context);

		this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);

		// 히스토리 적재
		String message;
		if (workspaceRole.getRole().equalsIgnoreCase("MANAGER")) {
			message = this.messageSource.getMessage(
				"WORKSPACE_SET_MANAGER", new java.lang.String[] {masterUser.getNickname(), user.getNickname()}, locale);
		} else {
			message = this.messageSource.getMessage(
				"WORKSPACE_SET_MEMBER", new java.lang.String[] {masterUser.getNickname(), user.getNickname()}, locale);
		}
		saveHistotry(workspace, responseUserId, message);

	}

	private void saveHistotry(Workspace workspace, String userId, String message) {
		History history = History.builder()
			.workspace(workspace)
			.userId(userId)
			.message(message)
			.build();
		this.historyRepository.save(history);
	}

	private void userLicenseValidCheck(Boolean planRemote, Boolean planMake, Boolean planView) {
		if (!planRemote && !planMake && !planView) {
			throw new WorkspaceException(ErrorCode.ERR_INCORRECT_USER_LICENSE_INFO);
		}
	}

	/**
	 * 워크스페이스 소속 부여 : insert workspace_user table
	 *
	 * @param workspaceId - 소속 부여 대상 워크스페이스 uuid
	 * @param userId      - 소속 시킬 유저 uuid
	 * @return - 소속 된 워크스페이스 유저 객체
	 */
	public WorkspaceUser setWorkspaceUserInfo(String workspaceId, String userId) {
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUser workspaceUser = WorkspaceUser.builder()
			.userId(userId)
			.workspace(workspace)
			.build();
		this.workspaceUserRepository.save(workspaceUser);
		return workspaceUser;
	}

	public ApiResponse<List<WorkspaceNewMemberInfoDTO>> getWorkspaceNewUserInfo(String workspaceId) {

		List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findTop4ByWorkspace_UuidOrderByCreatedDateDesc(
			workspaceId);//최신 4명만 가져와서

		List<WorkspaceNewMemberInfoDTO> workspaceNewMemberInfoList = new ArrayList<>();
		workspaceUserList.stream().forEach(workspaceUser -> {
			UserInfoRestResponse userInfoRestResponse = userRestService.getUserInfoByUserId(workspaceUser.getUserId())
				.getData();
			WorkspaceNewMemberInfoDTO newMemberInfo = modelMapper.map(
				userInfoRestResponse, WorkspaceNewMemberInfoDTO.class);
			newMemberInfo.setJoinDate(workspaceUser.getCreatedDate());
			newMemberInfo.setRole(
				this.workspaceUserPermissionRepository.findByWorkspaceUser(workspaceUser).getWorkspaceRole().getRole());
			workspaceNewMemberInfoList.add(newMemberInfo);
		});
		return new ApiResponse<>(workspaceNewMemberInfoList);
	}

	public ApiResponse<WorkspaceInfoDTO> setWorkspace(WorkspaceUpdateRequest workspaceUpdateRequest, Locale locale) {
		if (!StringUtils.hasText(workspaceUpdateRequest.getUserId()) || !StringUtils.hasText(
			workspaceUpdateRequest.getName())
			|| !StringUtils.hasText(workspaceUpdateRequest.getDescription()) || !StringUtils.hasText(
			workspaceUpdateRequest.getWorkspaceId())) {
			throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}

		//마스터 유저 체크
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceUpdateRequest.getWorkspaceId())
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		String oldWorkspaceName = workspace.getName();
		if (!workspace.getUserId().equals(workspaceUpdateRequest.getUserId())) {
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}

		if (!oldWorkspaceName.equals(workspaceUpdateRequest.getName())) {
			List<String> receiverEmailList = new ArrayList<>();
			Context context = new Context();
			context.setVariable("beforeWorkspaceName", oldWorkspaceName);
			context.setVariable("afterWorkspaceName", workspaceUpdateRequest.getName());
			context.setVariable("supportUrl", supportUrl);

			List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(
				workspace.getUuid());
			workspaceUserList.stream().forEach(workspaceUser -> {
				UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(
					workspaceUser.getUserId()).getData();
				receiverEmailList.add(userInfoRestResponse.getEmail());
				if (userInfoRestResponse.getUuid().equals(workspace.getUserId())) {
					context.setVariable("workspaceMasterNickName", userInfoRestResponse.getNickname());

				}
			});

			String subject = this.messageSource.getMessage(Mail.WORKSPACE_INFO_UPDATE.getSubject(), null, locale);
			String template = this.messageSource.getMessage(Mail.WORKSPACE_INFO_UPDATE.getTemplate(), null, locale);
			String html = springTemplateEngine.process(template, context);
			this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);
		}
		workspace.setName(workspaceUpdateRequest.getName());
		workspace.setDescription(workspaceUpdateRequest.getDescription());

		if (workspaceUpdateRequest.getProfile() != null) {
			String oldProfile = workspace.getProfile();
			//기존 프로필 이미지 삭제
			if (StringUtils.hasText(oldProfile) && !oldProfile.contains("workspace-profile.png")) {
				this.fileUploadService.delete(oldProfile);
			}
			//새 프로필 이미지 등록
			try {
				workspace.setProfile(this.fileUploadService.upload(workspaceUpdateRequest.getProfile()));
			} catch (Exception e) {
				throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
			}

		}

		this.workspaceRepository.save(workspace);

		WorkspaceInfoDTO workspaceInfoDTO = modelMapper.map(workspace, WorkspaceInfoDTO.class);
		workspaceInfoDTO.setMasterUserId(workspace.getUserId());

		return new ApiResponse<>(workspaceInfoDTO);
	}

	public ApiResponse<UserInfoDTO> getMemberInfo(String workspaceId, String userId) {
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, userId);
		UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(userId).getData();

		UserInfoDTO userInfoDTO = modelMapper.map(userInfoRestResponse, UserInfoDTO.class);
		userInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
		userInfoDTO.setLicenseProducts(new String[0]);

		List<String> licenseProducts = new ArrayList<>();
		MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
			workspaceId, userId).getData();
		if (myLicenseInfoListResponse.getLicenseInfoList() != null && !myLicenseInfoListResponse.getLicenseInfoList()
			.isEmpty()) {
			myLicenseInfoListResponse.getLicenseInfoList().forEach(myLicenseInfoResponse -> {
				licenseProducts.add(myLicenseInfoResponse.getProductName());
			});
			userInfoDTO.setLicenseProducts(licenseProducts.toArray(new String[licenseProducts.size()]));
		}

		return new ApiResponse<>(userInfoDTO);
	}

	@Transactional
	public ApiResponse<Boolean> kickOutMember(
		String workspaceId, MemberKickOutRequest memberKickOutRequest, Locale locale
	) {
		log.debug(
			"[WORKSPACE KICK OUT USER] Workspace >> {}, Kickout User >> {}, Request User >> {}", workspaceId,
			memberKickOutRequest.getKickedUserId(),
			memberKickOutRequest.getUserId()
		);
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(workspace.getUserId())
			.getData();

		//내쫓는 자의 권한 확인(마스터, 매니저만 가능)
		WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, memberKickOutRequest.getUserId());
		if (workspaceUserPermission.getWorkspaceRole().getRole().equals("MEMBER")) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//내쫓기는 자의 권한 확인(매니저, 멤버만 가능)
		WorkspaceUserPermission kickedUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(
			workspace, memberKickOutRequest.getKickedUserId());
		if (kickedUserPermission.getWorkspaceRole().getRole().equals("MASTER")) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//매니저 유저는 멤버만 쫓아낼 수 있다.
		if (workspaceUserPermission.getWorkspaceRole().getRole().equals("MANAGER")) {
			if (kickedUserPermission.getWorkspaceRole().getRole().equals("MANAGER")) {
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
			}
		}

		//라이선스 해제
		MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
			workspaceId, memberKickOutRequest.getKickedUserId()).getData();
		if (myLicenseInfoListResponse.getLicenseInfoList() != null && !myLicenseInfoListResponse.getLicenseInfoList()
			.isEmpty()) {
			myLicenseInfoListResponse.getLicenseInfoList().stream().forEach(myLicenseInfoResponse -> {
				log.debug(
					"[WORKSPACE KICK OUT USER] Workspace User License Revoke. License Product Name >> {}",
					myLicenseInfoResponse.getProductName()
				);
				Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(
					workspaceId, memberKickOutRequest.getKickedUserId(), myLicenseInfoResponse.getProductName())
					.getData();

				if (!revokeResult) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
				}
			});
		}

		//workspace_user_permission 삭제(history 테이블 기록)
		this.workspaceUserPermissionRepository.delete(kickedUserPermission);
		log.debug("[WORKSPACE KICK OUT USER] Delete Workspace user permission info.");

		//workspace_user 삭제(history 테이블 기록)
		this.workspaceUserRepository.delete(kickedUserPermission.getWorkspaceUser());
		log.debug("[WORKSPACE KICK OUT USER] Delete Workspace user info.");

		//메일 발송
		Context context = new Context();
		context.setVariable("workspaceName", workspace.getName());
		context.setVariable("workspaceMasterNickName", userInfoRestResponse.getNickname());
		context.setVariable("workspaceMasterEmail", userInfoRestResponse.getEmail());
		context.setVariable("supportUrl", supportUrl);

		UserInfoRestResponse kickedUser = this.userRestService.getUserInfoByUserId(
			memberKickOutRequest.getKickedUserId()).getData();

		List<String> receiverEmailList = new ArrayList<>();
		receiverEmailList.add(kickedUser.getEmail());

		String subject = this.messageSource.getMessage(Mail.WORKSPACE_KICKOUT.getSubject(), null, locale);
		String template = this.messageSource.getMessage(Mail.WORKSPACE_KICKOUT.getTemplate(), null, locale);
		String html = springTemplateEngine.process(template, context);
		this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);
		log.debug("[WORKSPACE KICK OUT USER] Send Workspace kick out mail.");

		//history 저장
		String message = this.messageSource.getMessage(
			"WORKSPACE_EXPELED", new String[] {userInfoRestResponse.getNickname(), kickedUser.getNickname()}, locale);
		History history = History.builder()
			.message(message)
			.userId(kickedUser.getUuid())
			.workspace(workspace)
			.build();
		this.historyRepository.save(history);

		return new ApiResponse<>(true);
	}

	public ApiResponse<Boolean> exitWorkspace(String workspaceId, String userId, Locale locale) {
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

		//마스터 유저는 워크스페이스 나가기를 할 수 없음.
		if (workspace.getUserId().equals(userId)) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		WorkspaceUser workspaceUser = this.workspaceUserRepository.findByUserIdAndWorkspace(userId, workspace);
		WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser(
			workspaceUser);

		//라이선스 해제
		MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(
			workspaceId, userId).getData();
		if (myLicenseInfoListResponse.getLicenseInfoList() != null && !myLicenseInfoListResponse.getLicenseInfoList()
			.isEmpty()) {
			myLicenseInfoListResponse.getLicenseInfoList().stream().forEach(myLicenseInfoResponse -> {
				Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(
					workspaceId, userId, myLicenseInfoResponse.getProductName()).getData();
				if (!revokeResult) {
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
				}
			});
		}

		this.workspaceUserPermissionRepository.delete(workspaceUserPermission);
		this.workspaceUserRepository.delete(workspaceUser);

		//history 저장
		UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(userId).getData();
		String message = this.messageSource.getMessage(
			"WORKSPACE_LEAVE", new String[] {userInfoRestResponse.getNickname()}, locale);
		History history = History.builder()
			.message(message)
			.userId(userId)
			.workspace(workspace)
			.build();
		this.historyRepository.save(history);

		return new ApiResponse<>(true);
	}

	public ApiResponse<Boolean> testSetMember(String workspaceId, WorkspaceInviteRequest workspaceInviteRequest) {
		Workspace workspace = this.workspaceRepository.findByUuid(workspaceId)
			.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
		List<String> emailList = new ArrayList<>();
		workspaceInviteRequest.getUserInfoList().stream().forEach(userInfo -> {
			emailList.add(userInfo.getEmail());
		});
		InviteUserInfoRestResponse responseUserList = this.userRestService.getUserInfoByEmailList(
			emailList.stream().toArray(String[]::new)).getData();

		responseUserList.getInviteUserInfoList().forEach(inviteUserResponse -> {
			if (!workspace.getUserId().equals(inviteUserResponse.getUserUUID())) {
				//workspaceUser set
				WorkspaceUser workspaceUser = WorkspaceUser.builder()
					.userId(inviteUserResponse.getUserUUID())
					.workspace(workspace)
					.build();
				this.workspaceUserRepository.save(workspaceUser);

				//workspaceUserPermission set
				WorkspaceRole workspaceRole = this.workspaceRoleRepository.findByRole("MEMBER");
				WorkspacePermission workspacePermission = WorkspacePermission.builder()
					.id(Permission.ALL.getValue())
					.build();
				WorkspaceUserPermission workspaceUserPermission = WorkspaceUserPermission.builder()
					.workspaceRole(workspaceRole)
					.workspaceUser(workspaceUser)
					.workspacePermission(workspacePermission)
					.build();
				this.workspaceUserPermissionRepository.save(workspaceUserPermission);
			}
		});

		return new ApiResponse<>(true);
	}

	public ApiResponse<WorkspaceHistoryListResponse> getWorkspaceHistory(
		String workspaceId, String userId, Pageable pageable
	) {

		Page<History> historyPage = this.historyRepository.findAllByUserIdAndWorkspace_Uuid(
			userId, workspaceId, pageable);
		List<WorkspaceHistoryListResponse.WorkspaceHistory> workspaceHistoryList = historyPage.stream().map(history -> {
			return modelMapper.map(history, WorkspaceHistoryListResponse.WorkspaceHistory.class);
		}).collect(Collectors.toList());

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(historyPage.getTotalElements());
		pageMetadataResponse.setTotalPage(historyPage.getTotalPages());
		pageMetadataResponse.setCurrentPage(pageable.getPageNumber());
		pageMetadataResponse.setCurrentSize(pageable.getPageSize());

		return new ApiResponse<>(new WorkspaceHistoryListResponse(workspaceHistoryList, pageMetadataResponse));
	}

	public ApiResponse<MemberListResponse> getSimpleWorkspaceUserList(String workspaceId) {
		List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId);
		String[] workspaceUserIdList = workspaceUserList.stream()
			.map(workspaceUser -> workspaceUser.getUserId())
			.toArray(String[]::new);
		List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();

		UserInfoListRestResponse userInfoListRestResponse = this.userRestService.getUserInfoList(
			"", workspaceUserIdList).getData();
		userInfoListRestResponse.getUserInfoList().stream().forEach(userInfoRestResponse -> {
			MemberInfoDTO memberInfoDTO = this.modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
			memberInfoDTOList.add(memberInfoDTO);
		});

		return new ApiResponse<>(new MemberListResponse(memberInfoDTOList, null));
	}

	/**
	 * 워크스페이스 소속 멤버 플랜 리스트 조회
	 *
	 * @param workspaceId - 조회 대상 워크스페이스 식별자
	 * @param pageable    -  페이징
	 * @return - 멤버 플랜 리스트
	 */
	public ApiResponse<WorkspaceUserLicenseListResponse> getLicenseWorkspaceUserList(
		String workspaceId, Pageable pageable
	) {
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(
			workspaceId).getData();
		if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null
			|| workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
			throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
		}

		List<WorkspaceUserLicenseInfoResponse> workspaceUserLicenseInfoList = new ArrayList<>();

		for (WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse licenseProductInfoResponse : workspaceLicensePlanInfoResponse
			.getLicenseProductInfoList()) {
			if (!licenseProductInfoResponse.getLicenseInfoList().isEmpty()) {
				for (WorkspaceLicensePlanInfoResponse.LicenseInfoResponse licenseInfoResponse : licenseProductInfoResponse
					.getLicenseInfoList()) {
					if (licenseInfoResponse.getStatus().equals(LicenseStatus.USE)) {
						UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(
							licenseInfoResponse.getUserId()).getData();
						WorkspaceUserLicenseInfoResponse workspaceUserLicenseInfo = new WorkspaceUserLicenseInfoResponse();
						workspaceUserLicenseInfo.setUuid(userInfoRestResponse.getUuid());
						workspaceUserLicenseInfo.setProfile(userInfoRestResponse.getProfile());
						workspaceUserLicenseInfo.setNickName(userInfoRestResponse.getNickname());
						workspaceUserLicenseInfo.setProductName(licenseProductInfoResponse.getProductName());
						workspaceUserLicenseInfo.setLicenseType(licenseProductInfoResponse.getLicenseType());
						workspaceUserLicenseInfoList.add(workspaceUserLicenseInfo);
					}
				}
			}
		}
		if (workspaceUserLicenseInfoList.isEmpty()) {
			PageMetadataRestResponse pageMetadataRestResponse = new PageMetadataRestResponse();
			pageMetadataRestResponse.setCurrentPage(pageable.getPageNumber());
			pageMetadataRestResponse.setCurrentSize(pageable.getPageSize());
			pageMetadataRestResponse.setTotalElements(0);
			pageMetadataRestResponse.setTotalPage(0);
			WorkspaceUserLicenseListResponse workspaceUserLicenseListResponse = new WorkspaceUserLicenseListResponse(
				workspaceUserLicenseInfoList, new PageMetadataRestResponse());
			return new ApiResponse<>(workspaceUserLicenseListResponse);
		}
		List<WorkspaceUserLicenseInfoResponse> beforeWorkspaceUserLicenseList = new ArrayList<>();

		//sort
		String sortName = pageable.getSort().toString().split(":")[0].trim();//sort의 기준이 될 열
		String sortDirection = pageable.getSort().toString().split(":")[1].trim();//sort의 방향 : 내림차순 or 오름차순

		if (sortName.equalsIgnoreCase("plan") && sortDirection.equalsIgnoreCase("asc")) {
			beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserLicenseInfoResponse::getProductName,
					Comparator.nullsFirst(Comparator.naturalOrder())
				))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("plan") && sortDirection.equalsIgnoreCase("desc")) {
			beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserLicenseInfoResponse::getProductName,
					Comparator.nullsFirst(Comparator.reverseOrder())
				))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("nickName") && sortDirection.equalsIgnoreCase("asc")) {
			beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserLicenseInfoResponse::getNickName,
					Comparator.nullsFirst(Comparator.naturalOrder())
				))
				.collect(Collectors.toList());
		}
		if (sortName.equalsIgnoreCase("nickName") && sortDirection.equalsIgnoreCase("desc")) {
			beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream()
				.sorted(Comparator.comparing(
					WorkspaceUserLicenseInfoResponse::getNickName,
					Comparator.nullsFirst(Comparator.reverseOrder())
				))
				.collect(Collectors.toList());
		}
		return new ApiResponse<>(
			paging(pageable.getPageNumber(), pageable.getPageSize(), beforeWorkspaceUserLicenseList));
	}

	public WorkspaceUserLicenseListResponse paging(
		int pageNum, int pageSize, List<WorkspaceUserLicenseInfoResponse> beforeWorkspaceUserLicenseList
	) {

		int totalElements = beforeWorkspaceUserLicenseList.size();
		int totalPage = totalElements / pageSize;
		int resultPage = totalPage;
		int lastElements = totalElements % pageSize;
		int currentPage = pageNum + 1;
		if (lastElements > 0) {
			totalPage = totalPage + 1;
			resultPage = resultPage + 1;
		}

		List<List<WorkspaceUserLicenseInfoResponse>> result = new ArrayList<>();

		int temp = 0;
		while (totalPage > 0) {
			List<WorkspaceUserLicenseInfoResponse> afterList = beforeWorkspaceUserLicenseList.stream()
				.skip(temp)
				.limit(pageSize)
				.collect(Collectors.toList());
			result.add(afterList);
			temp = temp + pageSize;
			totalPage = totalPage - 1;
		}

		PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
		pageMetadataResponse.setTotalElements(totalElements);
		pageMetadataResponse.setTotalPage(resultPage);
		pageMetadataResponse.setCurrentPage(currentPage);
		pageMetadataResponse.setCurrentSize(pageSize);
		if (currentPage > result.size()) {
			return new WorkspaceUserLicenseListResponse(new ArrayList<>(), pageMetadataResponse);
		}
		return new WorkspaceUserLicenseListResponse(result.get(pageNum), pageMetadataResponse);
	}

	public ApiResponse<WorkspaceLicenseInfoResponse> getWorkspaceLicenseInfo(String workspaceId) {
		WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(
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

		return new ApiResponse<>(workspaceLicenseInfoResponse);
	}

	/***
	 * 워크스페이스 정보 전체 삭제 처리
	 * @param workspaceUUID - 삭제할 워크스페이스의 마스터 사용자 식별자
	 * @return - 삭제 처리 결과
	 */
	@Transactional
	public WorkspaceSecessionResponse deleteAllWorkspaceInfo(String workspaceUUID) {
		Optional<Workspace> workspaceInfo = workspaceRepository.findByUuid(workspaceUUID);

		// workspace 정보가 없는 경우
		if (!workspaceInfo.isPresent()) {
			return new WorkspaceSecessionResponse(workspaceUUID, true, LocalDateTime.now());
		}

		Workspace workspace = workspaceInfo.get();

		List<WorkspaceUser> workspaceUserList = workspace.getWorkspaceUserList();

		// workspace user permission 삭제
		workspaceUserPermissionRepository.deleteAllWorkspaceUserPermissionByWorkspaceUser(workspaceUserList);

		// workspace user 삭제
		workspaceUserRepository.deleteAllWorkspaceUserByWorkspace(workspace);

		// workspace history 삭제
		historyRepository.deleteAllHistoryInfoByWorkspace(workspace);

		// workspace profile 삭제 (기본 이미지인 경우 제외)
		if (!workspace.getProfile().equals("default")) {
			fileUploadService.delete(workspace.getProfile());
		}

		// workspace 삭제
		workspaceRepository.delete(workspace);

		return new WorkspaceSecessionResponse(workspaceUUID, true, LocalDateTime.now());
	}

	@Transactional
	public WorkspaceMemberInfoListResponse createWorkspaceMemberAccount(
		String workspaceId, MemberAccountCreateRequest memberAccountCreateRequest
	) {
		//1. 요청한 사람의 권한 체크
		Workspace workspace = checkWorkspaceAndUserRole(
			workspaceId, memberAccountCreateRequest.getUserId(), new String[] {"MASTER", "MANAGER"});

		List<String> responseLicense = new ArrayList<>();
		List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();

		for (MemberAccountCreateInfo memberAccountCreateInfo : memberAccountCreateRequest.getMemberAccountCreateRequest()) {
			//1-1. 사용자에게 최소 1개 이상의 라이선스를 부여했는지 체크
			userLicenseValidCheck(memberAccountCreateInfo.getPlanRemote(), memberAccountCreateInfo.getPlanMake(),
				memberAccountCreateInfo.getPlanView()
			);

			//2. user-server 멤버 정보 등록 api 요청
			RegisterMemberRequest registerMemberRequest = new RegisterMemberRequest();
			registerMemberRequest.setEmail(memberAccountCreateInfo.getId());
			registerMemberRequest.setPassword(memberAccountCreateInfo.getPassword());
			UserInfoRestResponse userInfoRestResponse = userRestService.registerMemberRequest(
				registerMemberRequest,
				serviceID
			)
				.getData();

			if (userInfoRestResponse == null || !StringUtils.hasText(userInfoRestResponse.getUuid())) {
				log.error("[CREATE WORKSPACE MEMBER ACCOUNT] USER SERVER Member Register fail.");
				throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_CREATE_FAIL);
			}
			log.info(
				"[CREATE WORKSPACE MEMBER ACCOUNT] USER SERVER account register success. Create UUID : [{}], Create Date : [{}]",
				userInfoRestResponse.getUuid(), userInfoRestResponse.getCreatedDate()
			);

			//3. license-server grant api 요청 -> 실패시 user-server 롤백 api 요청
			if (memberAccountCreateInfo.getPlanRemote()) {
				MyLicenseInfoResponse myLicenseInfoResponse = licenseRestService.grantWorkspaceLicenseToUser(
					workspaceId, userInfoRestResponse.getUuid(), "REMOTE").getData();
				if (myLicenseInfoResponse == null || !StringUtils.hasText(myLicenseInfoResponse.getProductName())) {
					log.error(
						"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant fail. Request User UUID : [{}], Product License : [{}]",
						userInfoRestResponse.getUuid(), "REMOTE"
					);
					UserDeleteRestResponse userDeleteRestResponse = userRestService.userDeleteRequest(
						userInfoRestResponse.getUuid(), serviceID).getData();
					log.error(
						"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant fail >>>> USER SERVER account delete process. Request User UUID : [{}], Delete Date : [{}]",
						userDeleteRestResponse.getUserUUID(), userDeleteRestResponse.getDeletedDate()
					);
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
				}
				log.info(
					"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant success. Request User UUID : [{}], Product License : [{}]",
					userInfoRestResponse.getUuid(), myLicenseInfoResponse.getProductName()
				);
				responseLicense.add("REMOTE");
			}
			if (memberAccountCreateInfo.getPlanMake()) {
				MyLicenseInfoResponse myLicenseInfoResponse = licenseRestService.grantWorkspaceLicenseToUser(
					workspaceId, userInfoRestResponse.getUuid(), "MAKE").getData();
				if (myLicenseInfoResponse == null || !StringUtils.hasText(myLicenseInfoResponse.getProductName())) {
					log.error(
						"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant fail. Request User UUID : [{}], Product License : [{}]",
						userInfoRestResponse.getUuid(), "REMOTE"
					);
					UserDeleteRestResponse userDeleteRestResponse = userRestService.userDeleteRequest(
						userInfoRestResponse.getUuid(), serviceID).getData();
					log.error(
						"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant fail >>>> USER SERVER account delete process. Request User UUID : [{}], Delete Date : [{}]",
						userDeleteRestResponse.getUserUUID(), userDeleteRestResponse.getDeletedDate()
					);
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
				}
				log.info(
					"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant success. Request User UUID : [{}], Product License : [{}]",
					userInfoRestResponse.getUuid(), myLicenseInfoResponse.getProductName()
				);
				responseLicense.add("MAKE");
			}
			if (memberAccountCreateInfo.getPlanView()) {
				MyLicenseInfoResponse myLicenseInfoResponse = licenseRestService.grantWorkspaceLicenseToUser(
					workspaceId, userInfoRestResponse.getUuid(), "VIEW").getData();
				if (myLicenseInfoResponse == null || !StringUtils.hasText(myLicenseInfoResponse.getProductName())) {
					log.error(
						"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant fail. Request User UUID : [{}], Product License : [{}]",
						userInfoRestResponse.getUuid(), "REMOTE"
					);
					UserDeleteRestResponse userDeleteRestResponse = userRestService.userDeleteRequest(
						userInfoRestResponse.getUuid(), serviceID).getData();
					log.error(
						"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant fail >>>> USER SERVER account delete process. Request User UUID : [{}], Delete Date : [{}]",
						userDeleteRestResponse.getUserUUID(), userDeleteRestResponse.getDeletedDate()
					);
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
				}
				log.info(
					"[CREATE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license grant success. Request User UUID : [{}], Product License : [{}]",
					userInfoRestResponse.getUuid(), myLicenseInfoResponse.getProductName()
				);
				responseLicense.add("VIEW");
			}

			//4. workspace 권한 및 소속 부여
			WorkspaceUser newWorkspaceUser = WorkspaceUser.builder()
				.userId(userInfoRestResponse.getUuid())
				.workspace(workspace)
				.build();
			WorkspacePermission permission = workspacePermissionRepository.findById(1L).get();
			WorkspaceRole role = workspaceRoleRepository.findByRole(memberAccountCreateInfo.getRole());
			WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
				.workspaceUser(newWorkspaceUser)
				.workspacePermission(permission)
				.workspaceRole(role)
				.build();

			workspaceUserRepository.save(newWorkspaceUser);
			workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

			log.info(
				"[CREATE WORKSPACE MEMBER ACCOUNT] Workspace add user success. Request User UUID : [{}], Role : [{}], JoinDate : [{}]",
				userInfoRestResponse.getUuid(), role.getRole(), newWorkspaceUser.getCreatedDate()
			);

			//5. response
			MemberInfoDTO memberInfoResponse = modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
			memberInfoResponse.setRole(newWorkspaceUserPermission.getWorkspaceRole().getRole());
			memberInfoResponse.setRoleId(newWorkspaceUserPermission.getWorkspaceRole().getId());
			memberInfoResponse.setJoinDate(newWorkspaceUser.getCreatedDate());
			memberInfoResponse.setLicenseProducts(responseLicense.toArray(new String[responseLicense.size()]));
			memberInfoDTOList.add(memberInfoResponse);
		}

		return new WorkspaceMemberInfoListResponse(memberInfoDTOList);
	}

	@Transactional
	public boolean deleteWorkspaceMemberAccount(
		String workspaceId, MemberAccountDeleteRequest memberAccountDeleteRequest
	) {
		//1. 요청한 사람의 권한 체크
		checkWorkspaceAndUserRole(workspaceId, memberAccountDeleteRequest.getUserId(), new String[] {"MASTER"});

		//1-1. user-server로 권한 체크
		UserInfoRestResponse userInfoRestResponse = userRestService.getUserInfoByUserId(
			memberAccountDeleteRequest.getUserId()).getData();
		if (userInfoRestResponse == null || !StringUtils.hasText(userInfoRestResponse.getUuid())) {
			log.error(
				"[DELETE WORKSPACE MEMBER ACCOUNT] USER SERVER account not found. Request user UUID : [{}]",
				memberAccountDeleteRequest.getUserId()
			);
			throw new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
		}
		UserInfoAccessCheckRequest userInfoAccessCheckRequest = new UserInfoAccessCheckRequest();
		userInfoAccessCheckRequest.setEmail(userInfoRestResponse.getEmail());
		userInfoAccessCheckRequest.setPassword(memberAccountDeleteRequest.getUserPassword());
		UserInfoAccessCheckResponse userInfoAccessCheckResponse = userRestService.userInfoAccessCheckRequest(
			memberAccountDeleteRequest.getUserId(), userInfoAccessCheckRequest).getData();
		if (userInfoAccessCheckResponse == null || !userInfoAccessCheckResponse.isAccessCheckResult()) {
			log.error(
				"[DELETE WORKSPACE MEMBER ACCOUNT] USER SERVER account invalid. Request user UUID : [{}]",
				memberAccountDeleteRequest.getUserId()
			);
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}

		//2. license-sever revoke api 요청
		MyLicenseInfoListResponse myLicenseInfoListResponse = licenseRestService.getMyLicenseInfoRequestHandler(
			workspaceId, memberAccountDeleteRequest.getDeleteUserId()).getData();

		if (myLicenseInfoListResponse.getLicenseInfoList() != null && !myLicenseInfoListResponse.getLicenseInfoList()
			.isEmpty()) {
			myLicenseInfoListResponse.getLicenseInfoList().forEach(myLicenseInfoResponse -> {
				Boolean revokeResult = licenseRestService.revokeWorkspaceLicenseToUser(
					workspaceId,
					memberAccountDeleteRequest.getDeleteUserId(),
					myLicenseInfoResponse.getProductName()
				).getData();
				if (!revokeResult) {
					log.error(
						"[DELETE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license revoke fail. Request user UUID : [{}], Product License [{}]",
						memberAccountDeleteRequest.getUserId(),
						myLicenseInfoResponse.getProductName()
					);
					throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
				}
				log.info(
					"[DELETE WORKSPACE MEMBER ACCOUNT] LICENSE SERVER license revoke success. Request user UUID : [{}], Product License [{}]",
					memberAccountDeleteRequest.getUserId(),
					myLicenseInfoResponse.getProductName()
				);
			});
		}

		//3. user-server에 멤버 삭제 api 요청 -> 실패시 grant api 요청
		UserDeleteRestResponse userDeleteRestResponse = userRestService.userDeleteRequest(
			memberAccountDeleteRequest.getDeleteUserId(), serviceID).getData();
		if (userDeleteRestResponse == null || !StringUtils.hasText(userDeleteRestResponse.getUserUUID())) {
			log.error("[DELETE WORKSPACE MEMBER ACCOUNT] USER SERVER delete user fail.");
			if (myLicenseInfoListResponse.getLicenseInfoList() != null
				&& !myLicenseInfoListResponse.getLicenseInfoList().isEmpty()) {
				myLicenseInfoListResponse.getLicenseInfoList().forEach(myLicenseInfoResponse -> {
					MyLicenseInfoResponse grantResult = licenseRestService.grantWorkspaceLicenseToUser(
						workspaceId, memberAccountDeleteRequest.getDeleteUserId(),
						myLicenseInfoResponse.getProductName()
					).getData();
					log.error(
						"[DELETE WORKSPACE MEMBER ACCOUNT] USER SERVER delete user fail. >>>> LICENSE SERVER license revoke process. Request user UUID : [{}], Product License [{}]",
						memberAccountDeleteRequest.getDeleteUserId(), grantResult.getProductName()
					);
				});
			}
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ACCOUNT_DELETE_FAIL);
		}
		log.info(
			"[DELETE WORKSPACE MEMBER ACCOUNT] USER SERVER delete user success. Request user UUID : [{}],Delete Date [{}]",
			userDeleteRestResponse.getUserUUID(), userDeleteRestResponse.getDeletedDate()
		);

		//4. workspace-sever 권한 및 소속 해제
		Optional<Workspace> workspace = workspaceRepository.findByUuid(workspaceId);
		WorkspaceUser workspaceUser = workspaceUserRepository.findByUserIdAndWorkspace(
			memberAccountDeleteRequest.getDeleteUserId(), workspace.get());
		workspaceUserPermissionRepository.deleteAllByWorkspaceUser(workspaceUser);
		workspaceUserRepository.deleteById(workspaceUser.getId());

		log.info(
			"[DELETE WORKSPACE MEMBER ACCOUNT] Workspace delete user success. Request User UUID : [{}], Delete User UUID : [{}], DeleteDate : [{}]",
			memberAccountDeleteRequest.getUserId(), memberAccountDeleteRequest.getDeleteUserId(), LocalDateTime.now()
		);
		return true;
	}

	private Workspace checkWorkspaceAndUserRole(String workspaceId, String userId, String[] role) {
		Optional<Workspace> workspace = workspaceRepository.findByUuid(workspaceId);
		workspace.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

		Optional<WorkspaceUserPermission> workspaceUserPermission = workspaceUserPermissionRepository.findByWorkspaceUser_UserIdAndWorkspaceUser_Workspace(
			userId, workspace.get());
		workspaceUserPermission.orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_NOT_FOUND));

		log.info(
			"[CHECK WORKSPACE USER ROLE] Acceptable User Workspace Role : {}, Present User Role : [{}]",
			Arrays.toString(role),
			workspaceUserPermission.get().getWorkspaceRole().getRole()
		);
		if (!Arrays.asList(role)
			.stream()
			.anyMatch(workspaceUserPermission.get().getWorkspaceRole().getRole()::equals)) {
			throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
		}
		return workspace.get();
	}

	public WorkspacePaviconUpdateResponse updateWorkspacePavicon(
		String workspaceId, WorkspacePaviconUpdateRequest workspacePaviconUpdateRequest
	) {
		//1. 권한 체크
		Workspace workspace = checkWorkspaceAndUserRole(
			workspaceId, workspacePaviconUpdateRequest.getUserId(), new String[] {"MASTER"});

		//2. 파비콘 확장자 체크
		String extension = FilenameUtils.getExtension(workspacePaviconUpdateRequest.getPavicon().getOriginalFilename());
		if (!StringUtils.hasText(extension) || !extension.equalsIgnoreCase("ico")) {
			log.error(
				"[UPDATE WORKSAPCE PAVICON] Acceptable Image extension : [{}], Present Image extension : [{}] ",
				"ico", extension
			);
			throw new WorkspaceException(ErrorCode.ERR_NOT_ALLOW_FILE_EXTENSION);
		}
		//3. 파비콘 업로드
		try {
			String pavicon = fileUploadService.upload(workspacePaviconUpdateRequest.getPavicon());
			workspace.setPavicon(pavicon);
			workspaceRepository.save(workspace);

			WorkspacePaviconUpdateResponse workspacePaviconUpdateResponse = new WorkspacePaviconUpdateResponse();
			workspacePaviconUpdateResponse.setResult(true);
			workspacePaviconUpdateResponse.setPavicon(pavicon);
			return workspacePaviconUpdateResponse;
		} catch (IOException e) {
			log.error("[UPDATE WORKSAPCE PAVICON] Pavicon Image upload fail. Error message >> [{}]", e.getMessage());
			WorkspacePaviconUpdateResponse workspacePaviconUpdateResponse = new WorkspacePaviconUpdateResponse();
			workspacePaviconUpdateResponse.setResult(false);
			return workspacePaviconUpdateResponse;
		}
	}

	public WorkspaceLogoUpdateResponse updateWorkspaceLogo(
		String workspaceId, WorkspaceLogoUpdateRequest workspaceLogoUpdateRequest
	) {
		//1. 권한 체크
		Workspace workspace = checkWorkspaceAndUserRole(
			workspaceId, workspaceLogoUpdateRequest.getUserId(), new String[] {"MASTER"});

		//2. 로고 확장자 체크
		String allowExtension = "jpg,jpeg,gif,png";
		String defaultExtension = FilenameUtils.getExtension(
			workspaceLogoUpdateRequest.getDefaultLogo().getOriginalFilename());
		if (!StringUtils.hasText(defaultExtension) || !allowExtension.contains(defaultExtension.toLowerCase())) {
			log.error(
				"[UPDATE WORKSAPCE LOGO] Acceptable Image extension : [{}], Present Image extension : [{}] ",
				allowExtension, defaultExtension
			);
			throw new WorkspaceException(ErrorCode.ERR_NOT_ALLOW_FILE_EXTENSION);
		}
		try {
			String logo = fileUploadService.upload(workspaceLogoUpdateRequest.getDefaultLogo());
			workspace.setDefaultLogo(logo);
		} catch (IOException e) {
			log.error("[UPDATE WORKSAPCE LOGO] Logo Image upload fail. Error message >> [{}]", e.getMessage());
			WorkspaceLogoUpdateResponse workspaceLogoUpdateResponse = new WorkspaceLogoUpdateResponse();
			workspaceLogoUpdateResponse.setResult(false);
			return workspaceLogoUpdateResponse;
		}

		if (workspaceLogoUpdateRequest.getGreyLogo() != null) {
			String greyExtension = FilenameUtils.getExtension(
				workspaceLogoUpdateRequest.getGreyLogo().getOriginalFilename());
			if (!StringUtils.hasText(greyExtension) || !allowExtension.contains(greyExtension.toLowerCase())) {
				log.error(
					"[UPDATE WORKSAPCE LOGO] Acceptable Image extension : [{}], Present Image extension : [{}] ",
					allowExtension, greyExtension
				);
				throw new WorkspaceException(ErrorCode.ERR_NOT_ALLOW_FILE_EXTENSION);
			}
			try {
				String logo = fileUploadService.upload(workspaceLogoUpdateRequest.getGreyLogo());
				workspace.setGreyLogo(logo);
			} catch (IOException e) {
				log.error("[UPDATE WORKSAPCE LOGO] Logo Image upload fail. Error message >> [{}]", e.getMessage());
				WorkspaceLogoUpdateResponse workspaceLogoUpdateResponse = new WorkspaceLogoUpdateResponse();
				workspaceLogoUpdateResponse.setResult(false);
				return workspaceLogoUpdateResponse;
			}
		}
		if (workspaceLogoUpdateRequest.getWhiteLogo() != null) {
			String whiteExtension = FilenameUtils.getExtension(
				workspaceLogoUpdateRequest.getWhiteLogo().getOriginalFilename());
			if (!StringUtils.hasText(whiteExtension) || !allowExtension.contains(whiteExtension.toLowerCase())) {
				log.error(
					"[UPDATE WORKSAPCE LOGO] Acceptable Image extension : [{}], Present Image extension : [{}] ",
					allowExtension, whiteExtension
				);
				throw new WorkspaceException(ErrorCode.ERR_NOT_ALLOW_FILE_EXTENSION);
			}
			try {
				String logo = fileUploadService.upload(workspaceLogoUpdateRequest.getWhiteLogo());
				workspace.setWhiteLogo(logo);
			} catch (IOException e) {
				log.error("[UPDATE WORKSAPCE LOGO] Logo Image upload fail. Error message >> [{}]", e.getMessage());
				WorkspaceLogoUpdateResponse workspaceLogoUpdateResponse = new WorkspaceLogoUpdateResponse();
				workspaceLogoUpdateResponse.setResult(false);
				return workspaceLogoUpdateResponse;
			}
		}
		workspaceRepository.save(workspace);

		WorkspaceLogoUpdateResponse workspaceLogoUpdateResponse = new WorkspaceLogoUpdateResponse();
		workspaceLogoUpdateResponse.setResult(true);
		workspaceLogoUpdateResponse.setDefaultLogo(workspace.getDefaultLogo());
		workspaceLogoUpdateResponse.setGreyLogo(workspace.getGreyLogo());
		workspaceLogoUpdateResponse.setWhiteLogo(workspace.getWhiteLogo());
		return workspaceLogoUpdateResponse;
	}

	public WorkspaceTitleUpdateResponse updateWorkspaceTitle(
		String workspaceId, WorkspaceTitleUpdateRequest workspaceTitleUpdateRequest
	) {
		//1. 권한 체크
		Workspace workspace = checkWorkspaceAndUserRole(
			workspaceId, workspaceTitleUpdateRequest.getUserId(), new String[] {"MASTER"});

		//2. 고객사명 변경
		workspace.setTitle(workspaceTitleUpdateRequest.getTitle());
		workspaceRepository.save(workspace);

		WorkspaceTitleUpdateResponse workspaceTitleUpdateResponse = new WorkspaceTitleUpdateResponse();
		workspaceTitleUpdateResponse.setResult(true);
		workspaceTitleUpdateResponse.setTitle(workspace.getTitle());
		return workspaceTitleUpdateResponse;
	}

	public WorkspaceCustomSettingResponse getWorkspaceCustomSetting(String workspaceId) {
		Workspace workspace = workspaceRepository.findByUuid(workspaceId).orElseThrow(
			() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND)
		);
		WorkspaceCustomSettingResponse workspaceCustomSettingResponse = new WorkspaceCustomSettingResponse();
		workspaceCustomSettingResponse.setWorkspaceTitle(workspace.getTitle());
		workspaceCustomSettingResponse.setDefaultLogo(workspace.getDefaultLogo());
		workspaceCustomSettingResponse.setGreyLogo(workspace.getGreyLogo());
		workspaceCustomSettingResponse.setWhiteLogo(workspace.getWhiteLogo());
		workspaceCustomSettingResponse.setPavicon(workspace.getPavicon());

		return workspaceCustomSettingResponse;
	}
}

