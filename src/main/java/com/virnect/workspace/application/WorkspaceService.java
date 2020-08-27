package com.virnect.workspace.application;


import com.virnect.workspace.dao.*;
import com.virnect.workspace.dao.redis.UserInviteRepository;
import com.virnect.workspace.domain.*;
import com.virnect.workspace.domain.redis.UserInvite;
import com.virnect.workspace.dto.MemberInfoDTO;
import com.virnect.workspace.dto.UserInfoDTO;
import com.virnect.workspace.dto.WorkspaceInfoDTO;
import com.virnect.workspace.dto.WorkspaceNewMemberInfoDTO;
import com.virnect.workspace.dto.request.*;
import com.virnect.workspace.dto.response.*;
import com.virnect.workspace.dto.rest.*;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.constant.*;
import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.global.util.RandomStringTokenUtil;
import com.virnect.workspace.infra.file.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final FileUploadService fileUploadService;
    private final UserInviteRepository userInviteRepository;
    private final SpringTemplateEngine springTemplateEngine;
    private final HistoryRepository historyRepository;
    private final MessageSource messageSource;
    private final LicenseRestService licenseRestService;

    @Value("${file.upload-path}")
    private String fileUploadPath;

    @Value("${file.url}")
    private String fileUrl;

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

    /**
     * 워크스페이스 생성
     *
     * @param workspaceCreateRequest - 생성 할 워크스페이스 정보
     * @return - 생성 된 워크스페이스 정보
     */
    public ApiResponse<WorkspaceInfoDTO> createWorkspace(WorkspaceCreateRequest workspaceCreateRequest) {
        //필수 값 체크
        if (!StringUtils.hasText(workspaceCreateRequest.getUserId()) || !StringUtils.hasText(workspaceCreateRequest.getName()) || !StringUtils.hasText(workspaceCreateRequest.getDescription())) {
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
            profile = fileUrl + fileUploadPath + "workspace-profile.png";//디폴트 이미지 경로.
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
    public ApiResponse<WorkspaceInfoListResponse> getUserWorkspaces(String userId, Pageable pageable) {
        List<WorkspaceInfoListResponse.WorkspaceInfo> workspaceList = new ArrayList<>();
        Page<WorkspaceUser> workspaceUserPage = this.workspaceUserRepository.findByUserId(userId, pageable);
        for (WorkspaceUser workspaceUser : workspaceUserPage) {
            Workspace workspace = workspaceUser.getWorkspace();
            WorkspaceInfoListResponse.WorkspaceInfo workspaceInfo = modelMapper.map(workspace, WorkspaceInfoListResponse.WorkspaceInfo.class);

            WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser(workspaceUser);

            workspaceInfo.setJoinDate(workspaceUser.getCreatedDate());
            UserInfoRestResponse userInfoRestResponse = userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
            workspaceInfo.setMasterName(userInfoRestResponse.getName());
            workspaceInfo.setMasterProfile(userInfoRestResponse.getProfile());
            workspaceInfo.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
            workspaceInfo.setMasterNickName(userInfoRestResponse.getNickname());
            workspaceInfo.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());
            workspaceList.add(workspaceInfo);
        }

        PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();
        pageMetadataResponse.setTotalElements(workspaceUserPage.getTotalElements());
        pageMetadataResponse.setTotalPage(workspaceUserPage.getTotalPages());
        pageMetadataResponse.setCurrentPage(pageable.getPageNumber());
        pageMetadataResponse.setCurrentSize(pageable.getPageSize());

        //master-manager-member 순으로 고정 정렬
        workspaceList = workspaceList.stream().sorted(Comparator.comparing(WorkspaceInfoListResponse.WorkspaceInfo::getRoleId, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
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
    public ApiResponse<MemberListResponse> getMembers(String workspaceId, String search, String filter, com.virnect.workspace.global.common.PageRequest pageRequest) {
        boolean worksapcePlanExist = false;

        //Pageable로 Sort처리를 할 수 없기때문에 sort값을 제외한 Pageable을 만든다.
        Pageable newPageable = PageRequest.of(pageRequest.of().getPageNumber(), pageRequest.of().getPageSize());

        //filter set
        List<WorkspaceRole> workspaceRoleList = new ArrayList<>();
        if (StringUtils.hasText(filter) && filter.contains("MASTER")) {
            workspaceRoleList.add(WorkspaceRole.builder().id(1L).build());
        }
        if (StringUtils.hasText(filter) && filter.contains("MANAGER")) {
            workspaceRoleList.add(WorkspaceRole.builder().id(2L).build());
        }
        if (StringUtils.hasText(filter) && filter.contains("MEMBER")) {
            workspaceRoleList.add(WorkspaceRole.builder().id(3L).build());
        }

        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

        //USER-SERVER : 워크스페이스에 해당하는 유저들에 대한 정보만 불러온다. (+ search)
        List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId);
        String[] workspaceUserIdList = workspaceUserList.stream().map(workspaceUser -> workspaceUser.getUserId()).toArray(String[]::new);

        List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();
        PageMetadataRestResponse pageMetadataResponse = new PageMetadataRestResponse();

        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(workspaceId).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() != null) {
            worksapcePlanExist = true;
        }

        UserInfoListRestResponse userInfoListRestResponse = this.userRestService.getUserInfoList(search, workspaceUserIdList).getData();
        //불러온 정보들에서 userId 가지고 페이징 처리를 한다. (+ filter)
        if (!workspaceRoleList.isEmpty()) {
            List<WorkspaceUser> workspaceUsers = userInfoListRestResponse.getUserInfoList().stream().map(userInfoRestResponse -> {
                return this.workspaceUserRepository.findByUserIdAndWorkspace(userInfoRestResponse.getUuid(), workspace);

            }).collect(Collectors.toList());
            Page<WorkspaceUserPermission> workspaceUserPermissionPage = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUserIsInAndWorkspaceRoleIsIn(workspace, workspaceUsers, workspaceRoleList, newPageable);
            List<WorkspaceUserPermission> filterdWorkspaceUserList = workspaceUserPermissionPage.toList();

            for (UserInfoRestResponse userInfoRestResponse : userInfoListRestResponse.getUserInfoList()) {
                WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, userInfoRestResponse.getUuid());
                if (filterdWorkspaceUserList.contains(workspaceUserPermission)) {
                    MemberInfoDTO memberInfoDTO = this.modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
                    memberInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
                    memberInfoDTO.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());
                    memberInfoDTO.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());

                    String[] licenseProducts = new String[0];
                    if (worksapcePlanExist) {
                        MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(workspaceId, userInfoRestResponse.getUuid()).getData();
                        if (myLicenseInfoListResponse.getLicenseInfoList() != null) {
                            licenseProducts = myLicenseInfoListResponse.getLicenseInfoList().stream().map(myLicenseInfoResponse -> {
                                return myLicenseInfoResponse.getProductName();
                            }).toArray(String[]::new);
                            memberInfoDTO.setLicenseProducts(licenseProducts);
                        }
                    }
                    memberInfoDTO.setLicenseProducts(licenseProducts);
                    memberInfoDTOList.add(memberInfoDTO);
                }
            }
            pageMetadataResponse.setTotalElements(workspaceUserPermissionPage.getTotalElements());
            pageMetadataResponse.setTotalPage(workspaceUserPermissionPage.getTotalPages());
            pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
            pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());

        } else {
            List<String> userIdList = userInfoListRestResponse.getUserInfoList().stream().map(userInfoRestResponse -> userInfoRestResponse.getUuid()).collect(Collectors.toList());

            Page<WorkspaceUser> workspaceUserPage = this.workspaceUserRepository.findByWorkspace_UuidAndUserIdIn(workspaceId, userIdList, newPageable);
            List<WorkspaceUser> resultWorkspaceUser = workspaceUserPage.toList();

            for (UserInfoRestResponse userInfoRestResponse : userInfoListRestResponse.getUserInfoList()) {

                WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, userInfoRestResponse.getUuid());
                if (resultWorkspaceUser.contains(workspaceUserPermission.getWorkspaceUser())) {
                    MemberInfoDTO memberInfoDTO = this.modelMapper.map(userInfoRestResponse, MemberInfoDTO.class);
                    memberInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
                    memberInfoDTO.setRoleId(workspaceUserPermission.getWorkspaceRole().getId());
                    memberInfoDTO.setJoinDate(workspaceUserPermission.getWorkspaceUser().getCreatedDate());

                    String[] licenseProducts = new String[0];
                    if (worksapcePlanExist) {
                        MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(workspaceId, userInfoRestResponse.getUuid()).getData();
                        if (myLicenseInfoListResponse.getLicenseInfoList() != null) {
                            licenseProducts = myLicenseInfoListResponse.getLicenseInfoList().stream().map(myLicenseInfoResponse -> {
                                return myLicenseInfoResponse.getProductName();
                            }).toArray(String[]::new);
                            memberInfoDTO.setLicenseProducts(licenseProducts);
                        }
                    }
                    memberInfoDTO.setLicenseProducts(licenseProducts);
                    memberInfoDTOList.add(memberInfoDTO);
                }
            }
            pageMetadataResponse.setTotalElements(workspaceUserPage.getTotalElements());
            pageMetadataResponse.setTotalPage(workspaceUserPage.getTotalPages());
            pageMetadataResponse.setCurrentPage(pageRequest.of().getPageNumber() + 1);
            pageMetadataResponse.setCurrentSize(pageRequest.of().getPageSize());
        }
        List<MemberInfoDTO> resultMemberListResponse = new ArrayList<>();

        //sort처리
        resultMemberListResponse = getSortedMemberList(pageRequest, memberInfoDTOList);

        return new ApiResponse<>(new MemberListResponse(resultMemberListResponse, pageMetadataResponse));
    }

    public List<MemberInfoDTO> getSortedMemberList(com.virnect.workspace.global.common.PageRequest pageRequest, List<MemberInfoDTO> memberInfoDTOList) {

        String sortName = pageRequest.of().getSort().toString().split(":")[0].trim();//sort의 기준이 될 열
        String sortDirection = pageRequest.of().getSort().toString().split(":")[1].trim();//sort의 방향 : 내림차순 or 오름차순

        if (sortName.equalsIgnoreCase("role") && sortDirection.equalsIgnoreCase("asc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getRoleId, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("role") && sortDirection.equalsIgnoreCase("desc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getRoleId, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("email") && sortDirection.equalsIgnoreCase("asc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getEmail, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("email") && sortDirection.equalsIgnoreCase("desc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getEmail, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("joinDate") && sortDirection.equalsIgnoreCase("asc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getJoinDate, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("joinDate") && sortDirection.equalsIgnoreCase("desc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getJoinDate, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("nickname") && sortDirection.equalsIgnoreCase("asc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getNickName, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("nickname") && sortDirection.equalsIgnoreCase("desc")) {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getNickName, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
        } else {
            return memberInfoDTOList.stream().sorted(Comparator.comparing(MemberInfoDTO::getUpdatedDate, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());

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
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        WorkspaceInfoDTO workspaceInfo = modelMapper.map(workspace, WorkspaceInfoDTO.class);
        workspaceInfo.setMasterUserId(workspace.getUserId());

        //user 정보 set
        List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId);
        List<UserInfoDTO> userInfoList = new ArrayList<>();
        workspaceUserList.stream().forEach(workspaceUser -> {
            UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(workspaceUser.getUserId()).getData();
            UserInfoDTO userInfoDTO = modelMapper.map(userInfoRestResponse, UserInfoDTO.class);
            userInfoList.add(userInfoDTO);
            WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser(workspaceUser);
            userInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
        });

        //role 정보 set
        long masterUserCount = this.workspaceUserPermissionRepository.countByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MASTER");
        long managerUserCount = this.workspaceUserPermissionRepository.countByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        long memberUserCount = this.workspaceUserPermissionRepository.countByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MEMBER");

        //plan 정보 set
        int remotePlanCount = 0;
        int makePlanCount = 0;
        int viewPlanCount = 0;

        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(workspaceId).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() != null) {
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
        return new ApiResponse<>(new WorkspaceInfoResponse(workspaceInfo, userInfoList, masterUserCount, managerUserCount, memberUserCount, remotePlanCount, makePlanCount, viewPlanCount));
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
    public ApiResponse<Boolean> inviteWorkspace(String workspaceId, WorkspaceInviteRequest workspaceInviteRequest, Locale locale) {
        // 워크스페이스 플랜 조회하여 최대 초대 가능 명 수를 초과했는지 체크
        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(workspaceId).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null) {
            throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
        }
        int workspaceUserAmount = this.workspaceUserRepository.findByWorkspace_Uuid(workspaceId).size();
        if (workspaceLicensePlanInfoResponse.getMaxUserAmount() < workspaceUserAmount + workspaceInviteRequest.getUserInfoList().size()) {
            throw new WorkspaceException(ErrorCode.ERR_NOMORE_JOIN_WORKSPACE);
        }
        //초대받는 사람에게 할당할 라이선스가 있는 지 체크.
        long usefulRemote = workspaceLicensePlanInfoResponse.getLicenseProductInfoList().stream()
                .filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals(LicenseProduct.REMOTE.toString()))
                .map(licenseProductInfoResponse -> {
                    return licenseProductInfoResponse.getLicenseInfoList().stream().filter(licenseInfoResponse -> licenseInfoResponse.getStatus().equals("UNUSE"));
                }).count();

        long usefulMake = workspaceLicensePlanInfoResponse.getLicenseProductInfoList().stream()
                .filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals(LicenseProduct.MAKE.toString()))
                .map(licenseProductInfoResponse -> {
                    return licenseProductInfoResponse.getLicenseInfoList().stream().filter(licenseInfoResponse -> licenseInfoResponse.getStatus().equals("UNUSE"));
                }).count();

        long usefulView = workspaceLicensePlanInfoResponse.getLicenseProductInfoList().stream()
                .filter(licenseProductInfoResponse -> licenseProductInfoResponse.getProductName().equals(LicenseProduct.VIEW.toString()))
                .map(licenseProductInfoResponse -> {
                    return licenseProductInfoResponse.getLicenseInfoList().stream().filter(licenseInfoResponse -> licenseInfoResponse.getStatus().equals("UNUSE"));
                }).count();

        long requestRemote = 0, requestMake = 0, requestView = 0;

        for (WorkspaceInviteRequest.UserInfo userInfo : workspaceInviteRequest.getUserInfoList()) {
            //초대받은 사람의 유저의 권한은 매니저 또는 멤버만 가능하도록 체크x
            if (userInfo.getRole().equals("MASTER")) {
                throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
            }
            //초대받는 사람에게 부여되는 라이선스는 최소 1개 이상이도록 체크
            userLicenseValidCheck(userInfo.getPlanRemote(), userInfo.getPlanMake(), userInfo.getPlanView());

            if (userInfo.getPlanRemote()) {
                requestRemote = requestRemote + 1;
            }
            if (userInfo.getPlanMake()) {
                requestMake = requestMake + 1;
            }
            if (userInfo.getPlanView()) {
                requestView = requestView + 1;
            }
        }
        if (usefulRemote < requestRemote || usefulMake < requestMake || usefulView < requestView) {
            throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE);
        }

        //라이선스 플랜 타입 구하기 -- basic, pro..(한 워크스페이스에서 다른 타입의 라이선스 플랜을 동시에 가지고 있을 수 없으므로, 아무 플랜이나 잡고 타입을 구함.)
        String licensePlanType = workspaceLicensePlanInfoResponse.getLicenseProductInfoList().stream().map(licenseProductInfoResponse -> licenseProductInfoResponse.getLicenseType()).collect(Collectors.toList()).get(0);

        // 요청한 사람이 마스터유저 또는 매니저유저인지 체크
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, workspaceInviteRequest.getUserId());
        if (workspaceUserPermission.getWorkspaceRole().getRole().equals("MEMBER")) {
            throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
        }

        // 초대할 유저의 계정 유효성 체크(user 서비스)
        List<String> emailList = new ArrayList<>();
        workspaceInviteRequest.getUserInfoList().stream().forEach(userInfo -> emailList.add(userInfo.getEmail()));
        InviteUserInfoRestResponse responseUserList = this.userRestService.getUserInfoByEmailList(emailList.stream().toArray(String[]::new)).getData();

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
            if (this.workspaceUserRepository.findByUserIdAndWorkspace(inviteUserResponse.getUserUUID(), workspace) != null) {
                throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ALREADY_EXIST);
            }
            workspaceInviteRequest.getUserInfoList().stream().forEach(userInfo -> {
                if (inviteUserResponse.getEmail().equals(userInfo.getEmail())) {
                    //redis 긁어서 이미 초대한 정보 있는지 확인하고, 있으면 시간과 초대 정보 업데이트
                    UserInvite userInvite = this.userInviteRepository.findById(inviteUserResponse.getUserUUID() + "-" + workspaceId).orElse(null);
                    if (userInvite != null) {
                        userInvite.setRole(userInfo.getRole());
                        userInvite.setPlanRemote(userInfo.getPlanRemote());
                        userInvite.setPlanMake(userInfo.getPlanMake());
                        userInvite.setPlanView(userInfo.getPlanView());
                        userInvite.setUpdatedDate(LocalDateTime.now());
                        userInvite.setExpireTime(duration);
                        this.userInviteRepository.save(userInvite);
                        log.info("REDIS UPDATE - {}", userInvite.toString());
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
                        log.info("REDIS SET - {}", newUserInvite.toString());
                    }
                    //메일은 이미 초대한 것 여부와 관계없이 발송한다.
                    String rejectUrl = serverUrl + "/workspaces/" + workspaceId + "/invite/accept?userId=" + inviteUserResponse.getUserUUID() + "&accept=false";
                    String acceptUrl = serverUrl + "/workspaces/" + workspaceId + "/invite/accept?userId=" + inviteUserResponse.getUserUUID() + "&accept=true";
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
                    context.setVariable("plan", generatePlanString(userInfo.getPlanRemote(), userInfo.getPlanMake(), userInfo.getPlanView()));
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

    public RedirectView inviteWorkspaceResult(String workspaceId, String userId, Boolean accept, Locale locale) {
        if (accept) {
            return this.inviteWorkspaceAccept(workspaceId, userId, locale);
        } else {
            return this.inviteWorkspaceReject(workspaceId, userId, locale);
        }
    }

    public RedirectView inviteWorkspaceAccept(String workspaceId, String userId, Locale locale) {
        UserInvite userInvite = this.userInviteRepository.findById(userId + "-" + workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_NOT_FOUND_INVITE_WORKSPACE_INFO));
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

        //메일 발송 수신자 : 마스터 유저, 매니저 유저
        List<String> emailReceiverList = new ArrayList<>();
        UserInfoRestResponse masterUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
        emailReceiverList.add(masterUser.getEmail());

        List<WorkspaceUserPermission> workspaceUserPermissionList = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (workspaceUserPermissionList != null) {
            workspaceUserPermissionList.stream().forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
                emailReceiverList.add(managerUser.getEmail());
            });
        }

        //이미 마스터, 매니저, 멤버로 소속되어 있는 워크스페이스 최대 개수 9개 체크
        if (this.workspaceUserRepository.countWorkspaceUsersByUserId(userId) > 8) {
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
        }

        //라이선스 플랜 - 라이선스 플랜 보유 체크, 멤버 제한 수 체크
        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(workspaceId).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null) {
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
            context.setVariable("plan", generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView()));
            context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
            context.setVariable("planMakeType", userInvite.getPlanMakeType());
            context.setVariable("planViewType", userInvite.getPlanViewType());
            context.setVariable("contactUrl", contactUrl);
            context.setVariable("workstationHomeUrl", redirectUrl);
            context.setVariable("supportUrl", supportUrl);

            String subject = this.messageSource.getMessage(Mail.WORKSPACE_OVER_MAX_USER_FAIL.getSubject(), null, locale);
            String template = this.messageSource.getMessage(Mail.WORKSPACE_OVER_MAX_USER_FAIL.getTemplate(), null, locale);
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
            MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(workspaceId, userId, LicenseProduct.REMOTE.toString()).getData();
            if (!grantResult.getProductName().equals(LicenseProduct.REMOTE.toString())) {
                planRemoteGrantResult = false;
                failPlan.append("REMOTE");
            } else {
                successPlan.append("REMOTE");
            }
        }
        if (userInvite.getPlanMake()) {
            MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(workspaceId, userId, LicenseProduct.MAKE.toString()).getData();
            if (!grantResult.getProductName().equals(LicenseProduct.MAKE.toString())) {
                planMakeGrantResult = false;
                failPlan.append(",MAKE");
            } else {
                successPlan.append(",MAKE");
            }
        }
        if (userInvite.getPlanView()) {
            MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(workspaceId, userId, LicenseProduct.VIEW.toString()).getData();
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
        context.setVariable("plan", generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView()));
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
            message = this.messageSource.getMessage("WORKSPACE_INVITE_MANAGER", new String[]{userInvite.getResponseUserNickName(), generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView())}, locale);
        } else {
            message = this.messageSource.getMessage("WORKSPACE_INVITE_MEMBER", new String[]{userInvite.getResponseUserNickName(), generatePlanString(userInvite.getPlanRemote(), userInvite.getPlanMake(), userInvite.getPlanView())}, locale);
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
        UserInvite userInvite = this.userInviteRepository.findById(userId + "-" + workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_NOT_FOUND_INVITE_WORKSPACE_INFO));
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

        //워크스페이스 초대 거절 메일 수신자 : 마스터, 매니저
        List<String> emailReceiverList = new ArrayList<>();
        UserInfoRestResponse masterUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
        emailReceiverList.add(masterUser.getEmail());

        List<WorkspaceUserPermission> workspaceUserPermissionList = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (workspaceUserPermissionList != null) {
            workspaceUserPermissionList.stream().forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
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

    public Resource downloadFile(String fileName) throws IOException {
        Path file = Paths.get(fileUploadPath).resolve(fileName);
        Resource resource = new UrlResource(file.toUri());
        if (resource.getFile().exists()) {
            return resource;
        } else {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }

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
    public ApiResponse<Boolean> reviseMemberInfo(String workspaceId, MemberUpdateRequest memberUpdateRequest, Locale locale) {

        Workspace workspace = workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        WorkspaceUserPermission userPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, memberUpdateRequest.getUserId());

        WorkspaceRole workspaceRole = this.workspaceRoleRepository.findByRole(memberUpdateRequest.getRole().toUpperCase());
        UserInfoRestResponse masterUser = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();
        UserInfoRestResponse user = this.userRestService.getUserInfoByUserId(memberUpdateRequest.getUserId()).getData();
        UserInfoRestResponse requestUser = this.userRestService.getUserInfoByUserId(memberUpdateRequest.getRequestUserId()).getData();

        //권한 변경
        if (!userPermission.getWorkspaceRole().equals(workspaceRole)) {
            updateUserPermission(workspace, memberUpdateRequest.getRequestUserId(), memberUpdateRequest.getUserId(), workspaceRole, masterUser, user, locale);
        }

        //플랜 변경
        workspaceUserLicenseHandling(memberUpdateRequest.getUserId(), workspace, masterUser, user, requestUser, memberUpdateRequest.getLicenseRemote(), memberUpdateRequest.getLicenseMake(), memberUpdateRequest.getLicenseView(), locale);

        return new ApiResponse<>(true);
    }

    private void workspaceUserLicenseHandling(String userId, Workspace workspace, UserInfoRestResponse masterUser, UserInfoRestResponse user, UserInfoRestResponse requestUser, Boolean remoteLicense, Boolean makeLicense, Boolean viewLicense, Locale locale) {
        userLicenseValidCheck(remoteLicense, makeLicense, viewLicense);

        //사용자의 예전 라이선스정보 가져오기
        MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(workspace.getUuid(), userId).getData();
        List<String> oldProductList = new ArrayList<>();
        if (myLicenseInfoListResponse.getLicenseInfoList() != null) {
            oldProductList = myLicenseInfoListResponse.getLicenseInfoList().stream().map(myLicenseInfoResponse -> myLicenseInfoResponse.getProductName()).collect(Collectors.toList());
        }
        List<LicenseProduct> notFoundProductList = new ArrayList<>();

        List<LicenseProduct> newProductList = new ArrayList<>();
        if (remoteLicense) {
            if (!oldProductList.contains(LicenseProduct.REMOTE.toString())) {
                //CASE1 : 기존에 없던 라이선스인데 사용하는 경우면
                newProductList.add(LicenseProduct.REMOTE);
                MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(workspace.getUuid(), userId, LicenseProduct.REMOTE.toString()).getData();
                if (!grantResult.getProductName().equals(LicenseProduct.REMOTE.toString())) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
                }
            }
            //CASE2 : 기존에 있던 라이선스인데 사용하는 경우면
        } else {
            //CASE3 : 기존에 있던 라이선스인데 사용안하는 경우면
            if (oldProductList.contains(LicenseProduct.REMOTE.toString())) {
                notFoundProductList.add(LicenseProduct.REMOTE);
                Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(workspace.getUuid(), userId, LicenseProduct.REMOTE.toString()).getData();
                if (!revokeResult) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
                }
            }
            //CASE4 : 기존에 없던 라이선스인데 사용안하는 경우면
        }

        if (makeLicense) {
            if (!oldProductList.contains(LicenseProduct.MAKE.toString())) {
                newProductList.add(LicenseProduct.MAKE);
                MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(workspace.getUuid(), userId, LicenseProduct.MAKE.toString()).getData();
                if (!grantResult.getProductName().equals(LicenseProduct.MAKE.toString())) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
                }
            }
        } else {
            if (oldProductList.contains(LicenseProduct.MAKE.toString())) {
                notFoundProductList.add(LicenseProduct.MAKE);
                Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(workspace.getUuid(), userId, LicenseProduct.MAKE.toString()).getData();
                if (!revokeResult) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
                }
            }
        }

        if (viewLicense) {
            if (!oldProductList.contains(LicenseProduct.VIEW.toString())) {
                newProductList.add(LicenseProduct.VIEW);
                MyLicenseInfoResponse grantResult = this.licenseRestService.grantWorkspaceLicenseToUser(workspace.getUuid(), userId, LicenseProduct.VIEW.toString()).getData();
                if (!grantResult.getProductName().equals(LicenseProduct.VIEW.toString())) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL);
                }
            }
        } else {
            if (oldProductList.contains(LicenseProduct.VIEW.toString())) {
                notFoundProductList.add(LicenseProduct.VIEW);
                Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(workspace.getUuid(), userId, LicenseProduct.VIEW.toString()).getData();
                if (!revokeResult) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
                }
            }
        }

        if (!newProductList.isEmpty() || !notFoundProductList.isEmpty()) {
            //히스토리
            if (!newProductList.isEmpty()) {
                String newProductString = newProductList.stream().map(licenseProduct -> String.valueOf(licenseProduct)).collect(Collectors.joining());
                String message = this.messageSource.getMessage("WORKSPACE_GRANT_LICENSE", new java.lang.String[]{requestUser.getNickname(), user.getNickname(), newProductString}, locale);
                saveHistotry(workspace, userId, message);
            }
            if (!notFoundProductList.isEmpty()) {
                String notFoundProductString = notFoundProductList.stream().map(licenseProduct -> String.valueOf(licenseProduct)).collect(Collectors.joining());
                String message = this.messageSource.getMessage("WORKSPACE_REVOKE_LICENSE", new java.lang.String[]{requestUser.getNickname(), user.getNickname(), notFoundProductString}, locale);
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
            String template = this.messageSource.getMessage(Mail.WORKSPACE_USER_PLAN_UPDATE.getTemplate(), null, locale);
            String html = springTemplateEngine.process(template, context);
            this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);
        }

    }

    private void updateUserPermission(Workspace workspace, String requestUserId, String responseUserId, WorkspaceRole workspaceRole, UserInfoRestResponse masterUser, UserInfoRestResponse user, Locale locale) {
        //1. 요청자 권한 확인(마스터만 가능)
        WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, requestUserId);
        String role = workspaceUserPermission.getWorkspaceRole().getRole();
        if (role == null || !role.equalsIgnoreCase("MASTER")) {
            throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
        }

        //2. 대상자 권한 확인(매니저, 멤버 권한만 가능)
        WorkspaceUserPermission userPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, responseUserId);
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
        String subject = this.messageSource.getMessage(Mail.WORKSPACE_USER_PERMISSION_UPDATE.getSubject(), null, locale);
        String template = this.messageSource.getMessage(Mail.WORKSPACE_USER_PERMISSION_UPDATE.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);

        this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);

        // 히스토리 적재
        String message;
        if (workspaceRole.getRole().equalsIgnoreCase("MANAGER")) {
            message = this.messageSource.getMessage("WORKSPACE_SET_MANAGER", new java.lang.String[]{masterUser.getNickname(), user.getNickname()}, locale);
        } else {
            message = this.messageSource.getMessage("WORKSPACE_SET_MEMBER", new java.lang.String[]{masterUser.getNickname(), user.getNickname()}, locale);
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
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .userId(userId)
                .workspace(workspace)
                .build();
        this.workspaceUserRepository.save(workspaceUser);
        return workspaceUser;
    }

    public ApiResponse<List<WorkspaceNewMemberInfoDTO>> getWorkspaceNewUserInfo(String workspaceId) {

        List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findTop4ByWorkspace_UuidOrderByCreatedDateDesc(workspaceId);//최신 4명만 가져와서

        List<WorkspaceNewMemberInfoDTO> workspaceNewMemberInfoList = new ArrayList<>();
        workspaceUserList.stream().forEach(workspaceUser -> {
            UserInfoRestResponse userInfoRestResponse = userRestService.getUserInfoByUserId(workspaceUser.getUserId()).getData();
            WorkspaceNewMemberInfoDTO newMemberInfo = modelMapper.map(userInfoRestResponse, WorkspaceNewMemberInfoDTO.class);
            newMemberInfo.setJoinDate(workspaceUser.getCreatedDate());
            newMemberInfo.setRole(this.workspaceUserPermissionRepository.findByWorkspaceUser(workspaceUser).getWorkspaceRole().getRole());
            workspaceNewMemberInfoList.add(newMemberInfo);
        });
        return new ApiResponse<>(workspaceNewMemberInfoList);
    }

    public ApiResponse<WorkspaceInfoDTO> setWorkspace(WorkspaceUpdateRequest workspaceUpdateRequest, Locale locale) {
        if (!StringUtils.hasText(workspaceUpdateRequest.getUserId()) || !StringUtils.hasText(workspaceUpdateRequest.getName())
                || !StringUtils.hasText(workspaceUpdateRequest.getDescription()) || !StringUtils.hasText(workspaceUpdateRequest.getWorkspaceId())) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }

        //마스터 유저 체크
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceUpdateRequest.getWorkspaceId()).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
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

            List<WorkspaceUser> workspaceUserList = this.workspaceUserRepository.findByWorkspace_Uuid(workspace.getUuid());
            workspaceUserList.stream().forEach(workspaceUser -> {
                UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(workspaceUser.getUserId()).getData();
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
                this.fileUploadService.delete(oldProfile.substring(oldProfile.lastIndexOf("/") + 1));
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
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, userId);
        UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(userId).getData();


        UserInfoDTO userInfoDTO = modelMapper.map(userInfoRestResponse, UserInfoDTO.class);
        userInfoDTO.setRole(workspaceUserPermission.getWorkspaceRole().getRole());

        String[] licenseProducts = new String[0];
        MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(workspaceId, userId).getData();
        if (myLicenseInfoListResponse.getLicenseInfoList() != null) {
            licenseProducts = myLicenseInfoListResponse.getLicenseInfoList().stream().map(myLicenseInfoResponse -> {
                return myLicenseInfoResponse.getProductName();
            }).toArray(String[]::new);
            userInfoDTO.setLicenseProducts(licenseProducts);
        }

        return new ApiResponse<>(userInfoDTO);
    }

    public ApiResponse<Boolean> kickOutMember(String workspaceId, MemberKickOutRequest memberKickOutRequest, Locale locale) {
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(workspace.getUserId()).getData();

        //내쫓는 자의 권한 확인(마스터, 매니저만 가능)
        WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, memberKickOutRequest.getUserId());
        if (workspaceUserPermission.getWorkspaceRole().getRole().equals("MEMBER")) {
            throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
        }

        //내쫓기는 자의 권한 확인(매니저, 멤버만 가능)
        WorkspaceUserPermission kickedUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceUser_UserId(workspace, memberKickOutRequest.getKickedUserId());
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
        MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(workspaceId, memberKickOutRequest.getKickedUserId()).getData();
        if (myLicenseInfoListResponse.getLicenseInfoList() != null) {
            myLicenseInfoListResponse.getLicenseInfoList().stream().forEach(myLicenseInfoResponse -> {
                Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(workspaceId, memberKickOutRequest.getKickedUserId(), myLicenseInfoResponse.getProductName()).getData();
                if (!revokeResult) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
                }
            });
        }

        //workspace_user_permission 삭제(history 테이블 기록)
        this.workspaceUserPermissionRepository.delete(kickedUserPermission);

        //workspace_user 삭제(history 테이블 기록)
        this.workspaceUserRepository.delete(kickedUserPermission.getWorkspaceUser());

        //메일 발송
        Context context = new Context();
        context.setVariable("workspaceName", workspace.getName());
        context.setVariable("workspaceMasterNickName", userInfoRestResponse.getNickname());
        context.setVariable("workspaceMasterEmail", userInfoRestResponse.getEmail());
        context.setVariable("supportUrl", supportUrl);

        UserInfoRestResponse kickedUser = this.userRestService.getUserInfoByUserId(memberKickOutRequest.getKickedUserId()).getData();

        List<String> receiverEmailList = new ArrayList<>();
        receiverEmailList.add(kickedUser.getEmail());

        String subject = this.messageSource.getMessage(Mail.WORKSPACE_KICKOUT.getSubject(), null, locale);
        String template = this.messageSource.getMessage(Mail.WORKSPACE_KICKOUT.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);
        this.sendMailRequest(html, receiverEmailList, MailSender.MASTER.getValue(), subject);

        //history 저장
        String message = this.messageSource.getMessage("WORKSPACE_EXPELED", new String[]{userInfoRestResponse.getNickname(), kickedUser.getNickname()}, locale);
        History history = History.builder()
                .message(message)
                .userId(kickedUser.getUuid())
                .workspace(workspace)
                .build();
        this.historyRepository.save(history);

        return new ApiResponse<>(true);
    }

    public ApiResponse<Boolean> exitWorkspace(String workspaceId, String userId, Locale locale) {
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

        //마스터 유저는 워크스페이스 나가기를 할 수 없음.
        if (workspace.getUserId().equals(userId)) {
            throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
        }

        WorkspaceUser workspaceUser = this.workspaceUserRepository.findByUserIdAndWorkspace(userId, workspace);
        WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser(workspaceUser);

        //라이선스 해제
        MyLicenseInfoListResponse myLicenseInfoListResponse = this.licenseRestService.getMyLicenseInfoRequestHandler(workspaceId, userId).getData();
        if (myLicenseInfoListResponse.getLicenseInfoList() != null) {
            myLicenseInfoListResponse.getLicenseInfoList().stream().forEach(myLicenseInfoResponse -> {
                Boolean revokeResult = this.licenseRestService.revokeWorkspaceLicenseToUser(workspaceId, userId, myLicenseInfoResponse.getProductName()).getData();
                if (!revokeResult) {
                    throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL);
                }
            });
        }

        this.workspaceUserPermissionRepository.delete(workspaceUserPermission);
        this.workspaceUserRepository.delete(workspaceUser);

        //history 저장
        UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(userId).getData();
        String message = this.messageSource.getMessage("WORKSPACE_LEAVE", new String[]{userInfoRestResponse.getNickname()}, locale);
        History history = History.builder()
                .message(message)
                .userId(userId)
                .workspace(workspace)
                .build();
        this.historyRepository.save(history);

        return new ApiResponse<>(true);
    }

    public ApiResponse<Boolean> testSetMember(String workspaceId, WorkspaceInviteRequest workspaceInviteRequest) {
        Workspace workspace = this.workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        List<String> emailList = new ArrayList<>();
        workspaceInviteRequest.getUserInfoList().stream().forEach(userInfo -> {
            emailList.add(userInfo.getEmail());
        });
        InviteUserInfoRestResponse responseUserList = this.userRestService.getUserInfoByEmailList(emailList.stream().toArray(String[]::new)).getData();

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
                WorkspacePermission workspacePermission = WorkspacePermission.builder().id(Permission.ALL.getValue()).build();
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

    public ApiResponse<WorkspaceHistoryListResponse> getWorkspaceHistory(String workspaceId, String userId, Pageable pageable) {

        Page<History> historyPage = this.historyRepository.findAllByUserIdAndWorkspace_Uuid(userId, workspaceId, pageable);
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
        String[] workspaceUserIdList = workspaceUserList.stream().map(workspaceUser -> workspaceUser.getUserId()).toArray(String[]::new);
        List<MemberInfoDTO> memberInfoDTOList = new ArrayList<>();

        UserInfoListRestResponse userInfoListRestResponse = this.userRestService.getUserInfoList("", workspaceUserIdList).getData();
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
    public ApiResponse<WorkspaceUserLicenseListResponse> getLicenseWorkspaceUserList(String workspaceId, Pageable pageable) {
        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(workspaceId).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null) {
            throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
        }

        List<WorkspaceUserLicenseInfoResponse> workspaceUserLicenseInfoList = new ArrayList<>();

        for (WorkspaceLicensePlanInfoResponse.LicenseProductInfoResponse licenseProductInfoResponse : workspaceLicensePlanInfoResponse.getLicenseProductInfoList()) {
            for (WorkspaceLicensePlanInfoResponse.LicenseInfoResponse licenseInfoResponse : licenseProductInfoResponse.getLicenseInfoList()) {
                if (licenseInfoResponse.getStatus().equals("USE")) {
                    UserInfoRestResponse userInfoRestResponse = this.userRestService.getUserInfoByUserId(licenseInfoResponse.getUserId()).getData();
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
        if (workspaceUserLicenseInfoList.isEmpty()) {
            PageMetadataRestResponse pageMetadataRestResponse = new PageMetadataRestResponse();
            pageMetadataRestResponse.setCurrentPage(pageable.getPageNumber());
            pageMetadataRestResponse.setCurrentSize(pageable.getPageSize());
            pageMetadataRestResponse.setTotalElements(0);
            pageMetadataRestResponse.setTotalPage(0);
            WorkspaceUserLicenseListResponse workspaceUserLicenseListResponse = new WorkspaceUserLicenseListResponse(workspaceUserLicenseInfoList, new PageMetadataRestResponse());
            return new ApiResponse<>(workspaceUserLicenseListResponse);
        }
        List<WorkspaceUserLicenseInfoResponse> beforeWorkspaceUserLicenseList = new ArrayList<>();

        //sort
        String sortName = pageable.getSort().toString().split(":")[0].trim();//sort의 기준이 될 열
        String sortDirection = pageable.getSort().toString().split(":")[1].trim();//sort의 방향 : 내림차순 or 오름차순

        if (sortName.equalsIgnoreCase("plan") && sortDirection.equalsIgnoreCase("asc")) {
            beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream().sorted(Comparator.comparing(WorkspaceUserLicenseInfoResponse::getProductName, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("plan") && sortDirection.equalsIgnoreCase("desc")) {
            beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream().sorted(Comparator.comparing(WorkspaceUserLicenseInfoResponse::getProductName, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("nickName") && sortDirection.equalsIgnoreCase("asc")) {
            beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream().sorted(Comparator.comparing(WorkspaceUserLicenseInfoResponse::getNickName, Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
        }
        if (sortName.equalsIgnoreCase("nickName") && sortDirection.equalsIgnoreCase("desc")) {
            beforeWorkspaceUserLicenseList = workspaceUserLicenseInfoList.stream().sorted(Comparator.comparing(WorkspaceUserLicenseInfoResponse::getNickName, Comparator.nullsFirst(Comparator.reverseOrder()))).collect(Collectors.toList());
        }
        return new ApiResponse<>(paging(pageable.getPageNumber(), pageable.getPageSize(), beforeWorkspaceUserLicenseList));
    }

    public WorkspaceUserLicenseListResponse paging(int pageNum, int pageSize, List<WorkspaceUserLicenseInfoResponse> beforeWorkspaceUserLicenseList) {

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
        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = this.licenseRestService.getWorkspaceLicenses(workspaceId).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null) {
            throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
        }
        WorkspaceLicenseInfoResponse workspaceLicenseInfoResponse = new WorkspaceLicenseInfoResponse();
        List<WorkspaceLicenseInfoResponse.LicenseInfo> licenseInfoList = workspaceLicensePlanInfoResponse.getLicenseProductInfoList().stream().map(licenseProductInfoResponse -> {
            WorkspaceLicenseInfoResponse.LicenseInfo licenseInfo = new WorkspaceLicenseInfoResponse.LicenseInfo();
            licenseInfo.setLicenseType(licenseProductInfoResponse.getLicenseType());
            licenseInfo.setProductName(licenseProductInfoResponse.getProductName());
            licenseInfo.setUseLicenseAmount(licenseProductInfoResponse.getUseLicenseAmount());
            licenseInfo.setLicenseAmount(licenseProductInfoResponse.getUnUseLicenseAmount() + licenseProductInfoResponse.getUseLicenseAmount());
            return licenseInfo;
        }).collect(Collectors.toList());

        workspaceLicenseInfoResponse.setLicenseInfoList(licenseInfoList);
        DecimalFormat decimalFormat = new DecimalFormat("0");
        long size = workspaceLicensePlanInfoResponse.getMaxStorageSize();
        workspaceLicenseInfoResponse.setMaxStorageSize(Long.parseLong(decimalFormat.format(size / 1024.0))); //MB -> GB
        workspaceLicenseInfoResponse.setMaxDownloadHit(workspaceLicensePlanInfoResponse.getMaxDownloadHit());
        workspaceLicenseInfoResponse.setMaxCallTime(workspaceLicenseInfoResponse.getMaxCallTime());

        return new ApiResponse<>(workspaceLicenseInfoResponse);
    }

    /***
     * 워크스페이스 정보 전체 삭제 처리
     * @param workspaceUUID - 삭제될 워크스페이스 식별자
     * @return
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
}
