package com.virnect.workspace.application.workspaceuser;

import com.virnect.workspace.application.license.LicenseRestService;
import com.virnect.workspace.application.message.MessageRestService;
import com.virnect.workspace.application.user.UserRestService;
import com.virnect.workspace.dao.cache.UserInviteRepository;
import com.virnect.workspace.dao.workspace.*;
import com.virnect.workspace.domain.redis.UserInvite;
import com.virnect.workspace.domain.workspace.*;
import com.virnect.workspace.dto.onpremise.MemberAccountCreateRequest;
import com.virnect.workspace.dto.request.MemberAccountDeleteRequest;
import com.virnect.workspace.dto.request.WorkspaceInviteRequest;
import com.virnect.workspace.dto.request.WorkspaceMemberPasswordChangeRequest;
import com.virnect.workspace.dto.response.WorkspaceMemberInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceMemberPasswordChangeResponse;
import com.virnect.workspace.dto.rest.*;
import com.virnect.workspace.event.cache.UserWorkspacesDeleteEvent;
import com.virnect.workspace.event.history.HistoryAddEvent;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.RedirectProperty;
import com.virnect.workspace.global.common.mapper.rest.RestMapStruct;
import com.virnect.workspace.global.constant.*;
import com.virnect.workspace.global.error.ErrorCode;
import com.virnect.workspace.global.util.RandomStringTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.RedirectView;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

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
public class OffPWorkspaceUserServiceImpl extends WorkspaceUserService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceRoleRepository workspaceRoleRepository;
    private final WorkspacePermissionRepository workspacePermissionRepository;
    private final WorkspaceUserPermissionRepository workspaceUserPermissionRepository;
    private final UserRestService userRestService;
    private final MessageRestService messageRestService;
    private final UserInviteRepository userInviteRepository;
    private final SpringTemplateEngine springTemplateEngine;
    private final MessageSource messageSource;
    private final LicenseRestService licenseRestService;
    private final RedirectProperty redirectProperty;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OffPWorkspaceUserServiceImpl(WorkspaceRepository workspaceRepository, WorkspaceUserRepository workspaceUserRepository, WorkspaceRoleRepository workspaceRoleRepository, WorkspaceUserPermissionRepository workspaceUserPermissionRepository, UserRestService userRestService, MessageRestService messageRestService, SpringTemplateEngine springTemplateEngine, MessageSource messageSource, LicenseRestService licenseRestService, RedirectProperty redirectProperty, RestMapStruct restMapStruct, ApplicationEventPublisher applicationEventPublisher, WorkspacePermissionRepository workspacePermissionRepository, UserInviteRepository userInviteRepository) {
        super(workspaceRepository, workspaceUserRepository, workspaceRoleRepository, workspaceUserPermissionRepository, userRestService, messageRestService, springTemplateEngine, messageSource, licenseRestService, redirectProperty, restMapStruct, applicationEventPublisher);
        this.workspaceRepository = workspaceRepository;
        this.workspaceUserRepository = workspaceUserRepository;
        this.workspaceRoleRepository = workspaceRoleRepository;
        this.workspacePermissionRepository = workspacePermissionRepository;
        this.workspaceUserPermissionRepository = workspaceUserPermissionRepository;
        this.userRestService = userRestService;
        this.messageRestService = messageRestService;
        this.userInviteRepository = userInviteRepository;
        this.springTemplateEngine = springTemplateEngine;
        this.messageSource = messageSource;
        this.licenseRestService = licenseRestService;
        this.redirectProperty = redirectProperty;
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @Override
    public ApiResponse<Boolean> inviteWorkspace(
            String workspaceId, WorkspaceInviteRequest workspaceInviteRequest, Locale locale
    ) {
        /**
         * 권한체크
         * 초대하는 사람 권한 - 마스터, 매니저만 가능
         * 초대받는 사람 권한 - 매니저, 멤버만 가능
         * 초대하는 사람이 매니저일때 - 멤버만 초대할 수 있음.
         */

        //1. 초대하는 유저 권한 체크
        Optional<WorkspaceUserPermission> requestUserPermission = workspaceUserPermissionRepository.findWorkspaceUser(workspaceId, workspaceInviteRequest.getUserId());
        if (!requestUserPermission.isPresent() || requestUserPermission.get().getWorkspaceRole().getRole().equals("MEMBER")) {
            throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
        }
        workspaceInviteRequest.getUserInfoList().forEach(userInfo -> {
            OffPWorkspaceUserServiceImpl.log.debug("[WORKSPACE INVITE USER] Invite request user role >> [{}], response user role >> [{}]", requestUserPermission.get().getWorkspaceRole().getRole(), userInfo.getRole());
            if (userInfo.getRole().equalsIgnoreCase("MASTER")) {
                throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
            }
            if (requestUserPermission.get().getWorkspaceRole().getRole().equals("MANAGER") && userInfo.getRole().equalsIgnoreCase("MANAGER")) {
                throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_INVALID_PERMISSION);
            }
        });

        //2. 초대 정보 저장
        workspaceInviteRequest.getUserInfoList().forEach(userInfo -> {
            InviteUserInfoResponse inviteUserResponse = getInviteUserInfoResponse(userInfo.getEmail());
            //2-1. 이미 워크스페이스에 소속된 유저인지 체크
            if (inviteUserResponse.isMemberUser() &&
                    workspaceUserRepository.findByUserIdAndWorkspace_Uuid(inviteUserResponse.getInviteUserDetailInfo().getUserUUID(), workspaceId).isPresent()) {
                throw new WorkspaceException(ErrorCode.ERR_WORKSPACE_USER_ALREADY_EXIST);
            }

            boolean inviteSessionExist = false;
            String sessionCode = RandomStringTokenUtil.generate(UUIDType.INVITE_CODE, 20);
            for (UserInvite userInvite : userInviteRepository.findAll()) {
                if (userInvite != null && userInvite.getWorkspaceId().equals(workspaceId) && userInvite.getInvitedUserEmail().equals(userInfo.getEmail())) {
                    inviteSessionExist = true;
                    userInvite.setRole(userInfo.getRole());
                    userInvite.setPlanRemote(userInfo.isPlanRemote());
                    userInvite.setPlanMake(userInfo.isPlanMake());
                    userInvite.setPlanView(userInfo.isPlanView());
                    userInvite.setUpdatedDate(LocalDateTime.now());
                    userInvite.setExpireTime(Duration.ofDays(7).getSeconds());
                    userInviteRepository.save(userInvite);
                    sessionCode = userInvite.getSessionCode();
                    OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE USER] Workspace Invite Info Redis Update >> {}", userInvite.toString());
                }
            }
            if (!inviteSessionExist) {
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
                        //               .planRemoteType(licensePlanType)
//                        .planMakeType(licensePlanType)
//                        .planViewType(licensePlanType)
                        .invitedDate(LocalDateTime.now())
                        .updatedDate(null)
                        .expireTime(Duration.ofDays(7).getSeconds())
                        .build();
                userInviteRepository.save(newUserInvite);
                OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE USER] Workspace Invite Info Redis Set >> {}", newUserInvite.toString());
            }

            //메일 전송
            String rejectUrl = redirectProperty.getWorkspaceServer() + "/workspaces/invite/" + sessionCode + "/reject?lang=" + locale.getLanguage();
            String acceptUrl = redirectProperty.getWorkspaceServer() + "/workspaces/invite/" + sessionCode + "/accept?lang=" + locale.getLanguage();
            Workspace workspace = workspaceRepository.findByUuid(workspaceId).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
            UserInfoRestResponse materUser = getUserInfoByUserId(workspace.getUserId());
            if (inviteUserResponse.isMemberUser()) {
                Context context = new Context();
                context.setVariable("rejectUrl", rejectUrl);
                context.setVariable("acceptUrl", acceptUrl);
                context.setVariable("workspaceMasterNickName", materUser.getNickname());
                context.setVariable("workspaceMasterEmail", materUser.getEmail());
                context.setVariable("workspaceName", workspace.getName());
                context.setVariable("responseUserName", inviteUserResponse.getInviteUserDetailInfo().getName());
                context.setVariable("responseUserEmail", inviteUserResponse.getInviteUserDetailInfo().getEmail());
                context.setVariable("responseUserNickName", inviteUserResponse.getInviteUserDetailInfo().getNickname());
                context.setVariable("role", userInfo.getRole());
                context.setVariable("plan", generatePlanString(userInfo.isPlanRemote(), userInfo.isPlanMake(), userInfo.isPlanView()));
                context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
                context.setVariable("supportUrl", redirectProperty.getSupportWeb());
                String subject = messageSource.getMessage(Mail.WORKSPACE_INVITE.getSubject(), null, locale);
                String template = messageSource.getMessage(Mail.WORKSPACE_INVITE.getTemplate(), null, locale);
                String html = springTemplateEngine.process(template, context);
                List<String> emailReceiverList = new ArrayList<>();
                emailReceiverList.add(userInfo.getEmail());
                sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);
            } else {
                Context context = new Context();
                context.setVariable("rejectUrl", rejectUrl);
                context.setVariable("acceptUrl", acceptUrl);
                context.setVariable("masterUserName", materUser.getName());
                context.setVariable("masterUserNickname", materUser.getNickname());
                context.setVariable("masterUserEmail", materUser.getEmail());
                context.setVariable("workspaceName", workspace.getName());
                context.setVariable("inviteUserEmail", userInfo.getEmail());
                context.setVariable("role", userInfo.getRole());
                context.setVariable("plan", generatePlanString(userInfo.isPlanRemote(), userInfo.isPlanMake(), userInfo.isPlanView()));
                context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
                context.setVariable("supportUrl", redirectProperty.getSupportWeb());
                String subject = messageSource.getMessage(Mail.WORKSPACE_INVITE_NON_USER.getSubject(), null, locale);
                String template = messageSource.getMessage(Mail.WORKSPACE_INVITE_NON_USER.getTemplate(), null, locale);
                String html = springTemplateEngine.process(template, context);
                List<String> emailReceiverList = new ArrayList<>();
                emailReceiverList.add(userInfo.getEmail());
                sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);
            }
        });
        return new ApiResponse<>(true);
    }

    /**
     * 유저 정보 조회(User Service)
     *
     * @param userId - 유저 uuid
     * @return - 유저 정보
     */
    private UserInfoRestResponse getUserInfoByUserId(String userId) {
        //todo : logging
        return userRestService.getUserInfoByUserId(userId).getData();
    }

    private InviteUserInfoResponse getInviteUserInfoResponse(String email) {
        //todo: logging
        return userRestService.getUserInfoByEmail(email).getData();
    }

    public RedirectView inviteWorkspaceAccept(String sessionCode, String lang) throws IOException {
        Locale locale = new Locale(lang, "");
        UserInvite userInvite = userInviteRepository.findById(sessionCode).orElse(null);
        if (userInvite == null) {
            OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE ACCEPT] Workspace invite session Info Not found. session code >> [{}]", sessionCode);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(redirectProperty.getWorkstationWeb() + "/?message=workspace.invite.invalid");
            redirectView.setContentType("application/json");
            return redirectView;
        }


        OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE ACCEPT] Workspace invite session Info >> [{}]", userInvite.toString());
        InviteUserInfoResponse inviteUserResponse = userRestService.getUserInfoByEmail(userInvite.getInvitedUserEmail()).getData();
        if (inviteUserResponse != null && !inviteUserResponse.isMemberUser()) {
            OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE ACCEPT] Invited User isMemberUser Info >> [{}]", inviteUserResponse.isMemberUser());
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(redirectProperty.getTermsWeb() + "?inviteSession=" + sessionCode + "&lang=" + lang + "&email=" + userInvite.getInvitedUserEmail());
            redirectView.setContentType("application/json");
            return redirectView;

        }
        //비회원일경우 초대 session정보에 uuid가 안들어가므로 user서버에서 조회해서 가져온다.
        InviteUserDetailInfoResponse inviteUserDetailInfoResponse = inviteUserResponse.getInviteUserDetailInfo();
        userInvite.setInvitedUserEmail(inviteUserDetailInfoResponse.getEmail());
        userInvite.setInvitedUserId(inviteUserDetailInfoResponse.getUserUUID());
        userInviteRepository.save(userInvite);

        applicationEventPublisher.publishEvent(new UserWorkspacesDeleteEvent(inviteUserDetailInfoResponse.getUserUUID()));

        Workspace workspace = workspaceRepository.findByUuid(userInvite.getWorkspaceId()).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));

        //워크스페이스 최대 초대가능한 멤버수 9명 체크
        /*if (workspaceUserRepository.findByWorkspace_Uuid(workspace.getUuid()).size() > 8) {
            return worksapceOverJoinFailHandler(workspace, userInvite, locale);
        }
*/
        //라이선스 체크 - 라이선스 플랜 보유 체크, 멤버 제한 수 체크
        WorkspaceLicensePlanInfoResponse workspaceLicensePlanInfoResponse = licenseRestService.getWorkspaceLicenses(workspace.getUuid()).getData();
        if (workspaceLicensePlanInfoResponse.getLicenseProductInfoList() == null || workspaceLicensePlanInfoResponse.getLicenseProductInfoList().isEmpty()) {
            throw new WorkspaceException(ErrorCode.ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN);
        }

        //라이선스 최대 멤버 수 초과 메일전송
        int workspaceUserAmount = workspaceUserRepository.findByWorkspace_Uuid(workspace.getUuid()).size();
        if (workspaceLicensePlanInfoResponse.getMaxUserAmount() < workspaceUserAmount + 1) {
            OffPWorkspaceUserServiceImpl.log.error("[WORKSPACE INVITE ACCEPT] Over Max Workspace Member amount. max user Amount >> [{}], exist user amount >> [{}]",
                    workspaceLicensePlanInfoResponse.getMaxUserAmount(),
                    workspaceUserAmount + 1);
            worksapceOverMaxUserFailHandler(workspace, userInvite, locale);
        }

        //플랜 할당.
        boolean licenseGrantResult = true;
        List<String> successPlan = new ArrayList<>();
        List<String> failPlan = new ArrayList<>();

        List<LicenseProduct> licenseProductList = generatePlanList(userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView());
        for (LicenseProduct licenseProduct : licenseProductList) {
            MyLicenseInfoResponse grantResult = licenseRestService.grantWorkspaceLicenseToUser(workspace.getUuid(), inviteUserDetailInfoResponse.getUserUUID(), licenseProduct.toString()).getData();
            if (grantResult == null || !StringUtils.hasText(grantResult.getProductName())) {
                failPlan.add(licenseProduct.toString());
                licenseGrantResult = false;
            } else {
                successPlan.add(licenseProduct.toString());
            }
        }
        if (!licenseGrantResult) {
            workspaceOverPlanFailHandler(workspace, userInvite, successPlan, failPlan, locale);
            successPlan.forEach(s -> {
                Boolean revokeResult = licenseRestService.revokeWorkspaceLicenseToUser(workspace.getUuid(), userInvite.getInvitedUserId(), s).getData();
                OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE ACCEPT] [{}] License Grant Fail. Revoke user License Result >> [{}]", s, revokeResult);
            });
        }
        //워크스페이스 소속 넣기 (workspace_user)
        WorkspaceUser workspaceUser = WorkspaceUser.builder().workspace(workspace).userId(userInvite.getInvitedUserId()).build();
        workspaceUserRepository.save(workspaceUser);

        //워크스페이스 권한 부여하기 (workspace_user_permission)
        WorkspaceRole workspaceRole = workspaceRoleRepository.findByRole(userInvite.getRole().toUpperCase()).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
        WorkspacePermission workspacePermission = workspacePermissionRepository.findById(Permission.ALL.getValue()).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR));
        WorkspaceUserPermission newWorkspaceUserPermission = WorkspaceUserPermission.builder()
                .workspaceUser(workspaceUser)
                .workspaceRole(workspaceRole)
                .workspacePermission(workspacePermission)
                .build();
        workspaceUserPermissionRepository.save(newWorkspaceUserPermission);

        //MAIL 발송
        UserInfoRestResponse inviteUserInfo = getUserInfoByUserId(userInvite.getInvitedUserId());
        UserInfoRestResponse masterUserInfo = getUserInfoByUserId(workspace.getUserId());
        Context context = new Context();
        context.setVariable("workspaceName", workspace.getName());
        context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
        context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
        context.setVariable("acceptUserNickName", inviteUserInfo.getNickname());
        context.setVariable("acceptUserEmail", userInvite.getInvitedUserEmail());
        context.setVariable("role", userInvite.getRole());
        context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
        context.setVariable("plan", generatePlanString(userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView()));
        context.setVariable("supportUrl", redirectProperty.getSupportWeb());

        String subject = messageSource.getMessage(Mail.WORKSPACE_INVITE_ACCEPT.getSubject(), null, locale);
        String template = messageSource.getMessage(Mail.WORKSPACE_INVITE_ACCEPT.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);
        List<String> emailReceiverList = new ArrayList<>();
        emailReceiverList.add(masterUserInfo.getEmail());
        List<WorkspaceUserPermission> managerUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (managerUserPermissionList != null && !managerUserPermissionList.isEmpty()) {
            managerUserPermissionList.forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUserInfo = getUserInfoByUserId(workspaceUserPermission.getWorkspaceUser().getUserId());
                emailReceiverList.add(managerUserInfo.getEmail());
            });
        }

        sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

        //redis 에서 삭제
        userInviteRepository.delete(userInvite);

        //history 저장
        if (workspaceRole.getRole().equalsIgnoreCase("MANAGER")) {
            String message = messageSource.getMessage("WORKSPACE_INVITE_MANAGER", new String[]{inviteUserInfo.getNickname(), generatePlanString(userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView())}, locale);
            applicationEventPublisher.publishEvent(new HistoryAddEvent(message, userInvite.getInvitedUserId(), workspace));
        } else {
            String message = messageSource.getMessage("WORKSPACE_INVITE_MEMBER", new String[]{inviteUserInfo.getNickname(), generatePlanString(userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView())}, locale);
            applicationEventPublisher.publishEvent(new HistoryAddEvent(message, userInvite.getInvitedUserId(), workspace));
        }

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectProperty.getWorkstationWeb());
        redirectView.setContentType("application/json");
        return redirectView;
    }

    private List<LicenseProduct> generatePlanList(boolean remote, boolean make, boolean view) {
        List<LicenseProduct> productList = new ArrayList<>();
        if (remote) {
            productList.add(LicenseProduct.REMOTE);
        }
        if (make) {
            productList.add(LicenseProduct.MAKE);
        }
        if (view) {
            productList.add(LicenseProduct.VIEW);
        }
        return productList;
    }

    private String generatePlanString(boolean remote, boolean make, boolean view) {
        return generatePlanList(remote, make, view).stream().map(Enum::toString).collect(Collectors.joining(","));
    }

    @Override
    public WorkspaceMemberInfoListResponse createWorkspaceMemberAccount(String workspaceId, MemberAccountCreateRequest memberAccountCreateRequest) {
        return null;
    }

    @Override
    public boolean deleteWorkspaceMemberAccount(String workspaceId, MemberAccountDeleteRequest memberAccountDeleteRequest) {
        return false;
    }

    @Override
    public WorkspaceMemberPasswordChangeResponse memberPasswordChange(WorkspaceMemberPasswordChangeRequest passwordChangeRequest, String workspaceId) {
        return null;
    }

    public RedirectView worksapceOverJoinFailHandler(Workspace workspace, UserInvite userInvite, Locale locale) {
        UserInfoRestResponse inviteUserInfo = getUserInfoByUserId(userInvite.getInvitedUserId());
        UserInfoRestResponse masterUserInfo = getUserInfoByUserId(workspace.getUserId());
        Context context = new Context();
        context.setVariable("workspaceName", workspace.getName());
        context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
        context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
        context.setVariable("userNickName", inviteUserInfo.getNickname());
        context.setVariable("userEmail", inviteUserInfo.getEmail());
        context.setVariable("plan", generatePlanString(userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView()));
        context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
        context.setVariable("planMakeType", userInvite.getPlanMakeType());
        context.setVariable("planViewType", userInvite.getPlanViewType());
        context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
        context.setVariable("workstationMembersUrl", redirectProperty.getMembersWeb());
        context.setVariable("supportUrl", redirectProperty.getSupportWeb());

        String subject = messageSource.getMessage(Mail.WORKSPACE_OVER_JOIN_FAIL.getSubject(), null, locale);
        String template = messageSource.getMessage(Mail.WORKSPACE_OVER_JOIN_FAIL.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);

        List<String> emailReceiverList = new ArrayList<>();
        emailReceiverList.add(masterUserInfo.getEmail());
        List<WorkspaceUserPermission> managerUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (managerUserPermissionList != null && !managerUserPermissionList.isEmpty()) {
            managerUserPermissionList.forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUserInfo = getUserInfoByUserId(workspaceUserPermission.getWorkspaceUser().getUserId());
                emailReceiverList.add(managerUserInfo.getEmail());
            });
        }

        sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

        userInviteRepository.delete(userInvite);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectProperty.getWorkstationWeb() + RedirectPath.WORKSPACE_OVER_JOIN_FAIL.getValue());
        redirectView.setContentType("application/json");
        return redirectView;
    }

    public RedirectView worksapceOverMaxUserFailHandler(Workspace workspace, UserInvite userInvite, Locale locale) {
        UserInfoRestResponse inviteUserInfo = getUserInfoByUserId(userInvite.getInvitedUserId());
        UserInfoRestResponse masterUserInfo = getUserInfoByUserId(workspace.getUserId());

        Context context = new Context();
        context.setVariable("workspaceName", workspace.getName());
        context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
        context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
        context.setVariable("userNickName", inviteUserInfo.getNickname());
        context.setVariable("userEmail", inviteUserInfo.getEmail());
        context.setVariable("plan", generatePlanString(userInvite.isPlanRemote(), userInvite.isPlanMake(), userInvite.isPlanView()));
        context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
        context.setVariable("planMakeType", userInvite.getPlanMakeType());
        context.setVariable("planViewType", userInvite.getPlanViewType());
        context.setVariable("contactUrl", redirectProperty.getContactWeb());
        context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
        context.setVariable("supportUrl", redirectProperty.getSupportWeb());
        String subject = messageSource.getMessage(Mail.WORKSPACE_OVER_MAX_USER_FAIL.getSubject(), null, locale);
        String template = messageSource.getMessage(Mail.WORKSPACE_OVER_MAX_USER_FAIL.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);
        List<String> emailReceiverList = new ArrayList<>();
        emailReceiverList.add(masterUserInfo.getEmail());
        List<WorkspaceUserPermission> managerUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (managerUserPermissionList != null && !managerUserPermissionList.isEmpty()) {
            managerUserPermissionList.forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUserInfo = getUserInfoByUserId(workspaceUserPermission.getWorkspaceUser().getUserId());
                emailReceiverList.add(managerUserInfo.getEmail());
            });
        }
        sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

        userInviteRepository.delete(userInvite);
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectProperty.getWorkstationWeb() + RedirectPath.WORKSPACE_OVER_MAX_USER_FAIL.getValue());
        redirectView.setContentType("application/json");
        return redirectView;
    }


    public RedirectView workspaceOverPlanFailHandler(Workspace workspace, UserInvite
            userInvite, List<String> successPlan, List<String> failPlan, Locale locale) {
        UserInfoRestResponse inviteUserInfo = getUserInfoByUserId(userInvite.getInvitedUserId());
        UserInfoRestResponse masterUserInfo = getUserInfoByUserId(workspace.getUserId());

        Context context = new Context();
        context.setVariable("workspaceName", workspace.getName());
        context.setVariable("workspaceMasterNickName", masterUserInfo.getNickname());
        context.setVariable("workspaceMasterEmail", masterUserInfo.getEmail());
        context.setVariable("userNickName", inviteUserInfo.getNickname());
        context.setVariable("userEmail", inviteUserInfo.getEmail());
        context.setVariable("successPlan", org.apache.commons.lang.StringUtils.join(successPlan, ","));
        context.setVariable("failPlan", org.apache.commons.lang.StringUtils.join(failPlan, ","));
        context.setVariable("planRemoteType", userInvite.getPlanRemoteType());
        context.setVariable("planMakeType", userInvite.getPlanMakeType());
        context.setVariable("planViewType", userInvite.getPlanViewType());
        context.setVariable("workstationHomeUrl", redirectProperty.getWorkstationWeb());
        context.setVariable("workstationMembersUrl", redirectProperty.getMembersWeb());
        context.setVariable("supportUrl", redirectProperty.getSupportWeb());

        String subject = messageSource.getMessage(Mail.WORKSPACE_OVER_PLAN_FAIL.getSubject(), null, locale);
        String template = messageSource.getMessage(Mail.WORKSPACE_OVER_PLAN_FAIL.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);
        List<String> emailReceiverList = new ArrayList<>();
        emailReceiverList.add(masterUserInfo.getEmail());
        List<WorkspaceUserPermission> managerUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (managerUserPermissionList != null && !managerUserPermissionList.isEmpty()) {
            managerUserPermissionList.forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUserInfo = getUserInfoByUserId(workspaceUserPermission.getWorkspaceUser().getUserId());
                emailReceiverList.add(managerUserInfo.getEmail());
            });
        }
        sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

        userInviteRepository.delete(userInvite);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectProperty.getWorkstationWeb() + RedirectPath.WORKSPACE_OVER_PLAN_FAIL.getValue());
        redirectView.setContentType("application/json");
        return redirectView;
    }

    public RedirectView inviteWorkspaceReject(String sessionCode, String lang) {
        Locale locale = new Locale(lang, "");
        UserInvite userInvite = userInviteRepository.findById(sessionCode).orElse(null);
        if (userInvite == null) {
            OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE REJECT] Workspace invite session Info Not found. session code >> [{}]", sessionCode);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(redirectProperty.getWorkstationWeb());
            redirectView.setContentType("application/json");
            return redirectView;
        }
        OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE REJECT] Workspace Invite Session Info >> [{}] ", userInvite);

        //비회원 거절은 메일 전송 안함.
        InviteUserInfoResponse inviteUserResponse = userRestService.getUserInfoByEmail(userInvite.getInvitedUserEmail()).getData();
        if (inviteUserResponse != null && !inviteUserResponse.isMemberUser()) {
            OffPWorkspaceUserServiceImpl.log.info("[WORKSPACE INVITE REJECT] Invited User isMemberUser Info >> [{}]", inviteUserResponse.isMemberUser());
            userInviteRepository.delete(userInvite);
            RedirectView redirectView = new RedirectView();
            redirectView.setUrl(redirectProperty.getWorkstationWeb());
            redirectView.setContentType("application/json");
            return redirectView;
        }
        //비회원일경우 초대 session정보에 uuid가 안들어가므로 user서버에서 조회해서 가져온다.
        InviteUserDetailInfoResponse inviteUserDetailInfoResponse = inviteUserResponse.getInviteUserDetailInfo();
        userInvite.setInvitedUserEmail(inviteUserDetailInfoResponse.getEmail());
        userInvite.setInvitedUserId(inviteUserDetailInfoResponse.getUserUUID());
        userInviteRepository.save(userInvite);

        userInviteRepository.delete(userInvite);

        //MAIL 발송
        Workspace workspace = workspaceRepository.findByUuid(userInvite.getWorkspaceId()).orElseThrow(() -> new WorkspaceException(ErrorCode.ERR_WORKSPACE_NOT_FOUND));
        UserInfoRestResponse inviterUserInfo = getUserInfoByUserId(userInvite.getInvitedUserId());
        UserInfoRestResponse masterUserInfo = getUserInfoByUserId(workspace.getUserId());
        Context context = new Context();
        context.setVariable("rejectUserNickname", inviterUserInfo.getNickname());
        context.setVariable("rejectUserEmail", userInvite.getInvitedUserEmail());
        context.setVariable("workspaceName", workspace.getName());
        context.setVariable("accountUrl", redirectProperty.getAccountWeb());
        context.setVariable("supportUrl", redirectProperty.getSupportWeb());

        String subject = messageSource.getMessage(Mail.WORKSPACE_INVITE_REJECT.getSubject(), null, locale);
        String template = messageSource.getMessage(Mail.WORKSPACE_INVITE_REJECT.getTemplate(), null, locale);
        String html = springTemplateEngine.process(template, context);

        List<String> emailReceiverList = new ArrayList<>();
        emailReceiverList.add(masterUserInfo.getEmail());

        List<WorkspaceUserPermission> managerUserPermissionList = workspaceUserPermissionRepository.findByWorkspaceUser_WorkspaceAndWorkspaceRole_Role(workspace, "MANAGER");
        if (managerUserPermissionList != null && !managerUserPermissionList.isEmpty()) {
            managerUserPermissionList.forEach(workspaceUserPermission -> {
                UserInfoRestResponse managerUserInfo = getUserInfoByUserId(workspaceUserPermission.getWorkspaceUser().getUserId());
                emailReceiverList.add(managerUserInfo.getEmail());
            });
        }
        sendMailRequest(html, emailReceiverList, MailSender.MASTER.getValue(), subject);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectProperty.getWorkstationWeb());
        redirectView.setContentType("application/json");
        return redirectView;
    }

    /**
     * pf-message 서버로 보낼 메일 전송 api body
     *
     * @param html      - 본문
     * @param receivers - 수신정보
     * @param sender    - 발신정보
     * @param subject   - 제목
     */
    private void sendMailRequest(String html, List<String> receivers, String sender, String subject) {
        MailRequest mailRequest = new MailRequest();
        mailRequest.setHtml(html);
        mailRequest.setReceivers(receivers);
        mailRequest.setSender(sender);
        mailRequest.setSubject(subject);
        messageRestService.sendMail(mailRequest);
    }


}
