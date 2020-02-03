package com.virnect.workspace.application;

import com.virnect.workspace.dao.*;
import com.virnect.workspace.domain.Workspace;
import com.virnect.workspace.domain.WorkspaceRole;
import com.virnect.workspace.domain.WorkspaceUser;
import com.virnect.workspace.domain.WorkspaceUserPermission;
import com.virnect.workspace.dto.UserDTO;
import com.virnect.workspace.dto.WorkspaceDTO;
import com.virnect.workspace.exception.BusinessException;
import com.virnect.workspace.global.common.ResponseMessage;
import com.virnect.workspace.global.constant.UUIDType;
import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.global.util.RandomStringTokenUtil;
import com.virnect.workspace.infra.mail.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final WorkspacePermissionRepository workspacePermissionRepository;
    private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
    private final UserRestService userRestService;
    private final ModelMapper modelMapper;
    private final GroupService groupService;
    private final MailService mailService;

    public ResponseMessage createWorkspace(WorkspaceDTO.WorkspaceCreateReq workspaceInfo) {
        if (getUserInfo(workspaceInfo.getUserId()).getUserType().equals("SUB_USER")) {
            throw new BusinessException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
        }

        //워크스페이스 생성 & 워크스페이스 자동 소속 & 워크스페이스 권한 마스터 등록
        String uuid = RandomStringTokenUtil.generate(UUIDType.UUID_WITH_SEQUENCE, 0);
        String pinNumber = RandomStringTokenUtil.generate(UUIDType.PIN_NUMBER, 0);

        Workspace newWorkspace = Workspace.builder()
                .uuid(uuid)
                .userId(workspaceInfo.getUserId())
                .description(workspaceInfo.getDescription())
                .pinNumber(pinNumber)
                .build();

        this.workspaceRepository.save(newWorkspace);

        WorkspaceUser newWorkspaceUser = WorkspaceUser.builder()
                .userId(workspaceInfo.getUserId())
                .workspace(newWorkspace)
                .build();
        this.workspaceUserRepository.save(newWorkspaceUser);

        WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
                .workspaceRole(workspaceRoleRepository.findById(1L).get())
                .workspacePermission(workspacePermissionRepository.findById(1L).get())
                .workspaceUser(newWorkspaceUser)
                .build();
        this.workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

        return new ResponseMessage().addParam("workspace", newWorkspace);
    }

    public ResponseMessage getUserWorkspaces(String userId) {
        List<WorkspaceDTO.WorkspaceInfoRes> userWorkspaceList = new ArrayList<>();
        this.workspaceUserRepository.findByUserId(userId).forEach(workspaceUser -> {
            Workspace workspace = workspaceUser.getWorkspace();
            WorkspaceDTO.WorkspaceInfoRes userWorkspaceInfo = modelMapper.map(workspace, WorkspaceDTO.WorkspaceInfoRes.class);
            WorkspaceUserPermission workspaceUserPermission = this.workspaceUserPermissionRepository.findByWorkspaceUser(workspaceUser);
            userWorkspaceInfo.setRole(workspaceUserPermission.getWorkspaceRole().getRole());
            userWorkspaceList.add(userWorkspaceInfo);
        });

        return new ResponseMessage().addParam("workspaceList", userWorkspaceList);
    }

    public ResponseMessage getMember(String workspaceId, String userId, String search, String filter, Pageable pageable) {

        //1. 검색어 검증, 정렬(UserService에서)
        ResponseMessage responseMessage = this.userRestService.getUserInfoListUserIdAndSearchKeyword(userId, search, pageable);
        Map<String, Object> data = responseMessage.getData();
        log.info("ResponseMessage: {}", data);
        List<Object> results = ((List<Object>) data.get("userInfoList"));
        List<UserDTO.UserInfoDTO> userInfoDTOList = results.stream().map(object -> modelMapper.map(object, UserDTO.UserInfoDTO.class)).collect(Collectors.toList());
        List<UserDTO.UserInfoDTO> result = new ArrayList<>();

        //2. 필터 검증
        if (StringUtils.hasText(filter) && filter.equals("MASTER")) {
            result = this.workspaceUserPermissionRepository.findUserInfoListFilterd(userInfoDTOList, workspaceId);
        } else {
            result = userInfoDTOList;
        }

        Map<String, Object> pageableResult = new HashMap<>();
        pageableResult.put("currentPage", pageable.getPageNumber());
        pageableResult.put("currentSize", pageable.getPageSize());
        pageableResult.put("totalPage", data.get("totalPage"));
        pageableResult.put("totalElements", result.size());
        return new ResponseMessage().addParam("memberList", result).addParam("Pageable", pageableResult);
    }

    /**
     * 워크스페이스 정보 조회(현재는 멤버수만 return 추후에.. +라이선스, +워크스페이스 Strorage 정보)
     *
     * @param workspaceId - 워크스페이스 uuid
     * @param userId      - 사용자 uuid
     * @return
     */
    public ResponseMessage getWorkspaceInfo(String workspaceId, String userId) {
        //마스터 인지 체크
        if (getUserInfo(userId).getUserType().equals("SUB_USER")) {
            throw new BusinessException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
        }
        //멤버 수 조회
        long countWorkspaceUsers = this.workspaceUserRepository.countWorkspaceUser(workspaceId);

        //라이선스 수 조회
        //제품 이용 멤버 수 조회(Remote, Make, View)
        //워크스페이스 Storage 수 조회

        return new ResponseMessage().addParam("workspaceMembers", countWorkspaceUsers);
    }

    /**
     * 유저 조회(User Service)
     *
     * @param userId - 유저 uuid
     * @return - 유저 목록
     */
    private UserDTO.UserInfoDTO getUserInfo(String userId) {
        ResponseMessage userServerResponse = this.userRestService.getUserInfoByUserId(userId);
        log.info("data: {}", userServerResponse.getData());
        UserDTO.UserInfoDTO requestUserInfo = modelMapper.map(userServerResponse.getData().get("userInfo"), UserDTO.UserInfoDTO.class);
        return requestUserInfo;

    }

    /**
     * 워크스페이스 유저 초대
     *
     * @param workspaceId              - 초대 할 워크스페이스 uuid
     * @param workspaceInviteMemberReq - 초대 유저 정보
     * @return
     */
    public ResponseMessage inviteWorkspace(String workspaceId, String userId, WorkspaceDTO.WorkspaceInviteMemberReq workspaceInviteMemberReq) {
        //1. 요청한 사람이 마스터유저 또는 매니저유저인지 체크
        if (getWorkspaceUserRole(workspaceId, userId).getRole().equals("MEMBER")) {
            throw new BusinessException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
        }
        //2. (옵션)라이선스검사 -> 라이선스 할당
        //3. 이미 존재하는 사용자인지 이메일 체크(user Server) & 이메일 발송

      /*  for (WorkspaceDTO.WorkspaceInviteMemberReq metaUserInfo : workspaceInviteMemberReq.getUserInfoList()) {
            this.mailService.sendStringMail(MailSender.MASTER.getSender(),metaUserInfo.getUserEmail(), MailSubject.MAIL_SUBJECT_PREFIX.getSubject(),
                    "test email");
        }*/
        //this.userRestService.getInviteUserInfo(emailList);
        //4. redis에 정보 넣기

        //4. 워크스페이스 소속 넣기 (workspace_user)
        /*for (WorkspaceDTO.WorkspaceInviteMemberReq userInfo : workspaceInviteMemberReq.getUserInfoList()) {
            WorkspaceUser workspaceUser = WorkspaceUser.builder()
                    .userId("e0a3608a-035f-408a-85ec-96e0542222")
                    .workspace(this.workspaceRepository.findByUuid(workspaceId))
                    .build();
            this.workspaceUserRepository.save(workspaceUser);

            //5. (매니저권한이면) 워크스페이스 권한 부여하기 (workspace_user_permission)
            if (userInfo.getWorkspacePermission().size() < 1) {
                for (long permissionId : userInfo.getWorkspacePermission()) {
                    WorkspaceUserPermission workspaceUserPermission = WorkspaceUserPermission.builder()
                            .workspaceRole(this.workspaceRoleRepository.findByRole("MANAGER"))
                            .workspacePermission(this.workspacePermissionRepository.findById(permissionId).get())
                            .workspaceUser(workspaceUser)
                            .build();
                    this.workspaceUserPermissionRepository.save(workspaceUserPermission);

                }
            }

            //6. 그룹 소속 넣기(group_user, group_user_permission)
            if (userInfo.getGroups().size() > 0) {
                this.groupService.setGroupUsers(userInfo.getGroups(), workspaceUser);
            }
        }*/
        return new ResponseMessage();
    }

    public WorkspaceRole getWorkspaceUserRole(String workspaceId, String userId) {
        return this.workspaceUserPermissionRepository.findWorkspaceUserRole(workspaceId, userId);
    }
}
