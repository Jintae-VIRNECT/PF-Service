package com.virnect.workspace.application;

import com.virnect.workspace.dao.GroupPermissionRepository;
import com.virnect.workspace.dao.GroupRepository;
import com.virnect.workspace.dao.group.GroupRoleRepository;
import com.virnect.workspace.dao.group.GroupUserPermissionRepository;
import com.virnect.workspace.dao.group.GroupUserRepository;
import com.virnect.workspace.domain.*;
import com.virnect.workspace.dto.GroupInfoDTO;
import com.virnect.workspace.exception.BusinessException;
import com.virnect.workspace.global.error.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-30
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupService {
    private final GroupUserRepository groupUserRepository;
    private final GroupRepository groupRepository;
    private final ModelMapper modelMapper;
    private final GroupUserPermissionRepository groupUserPermissionRepository;
    private final GroupRoleRepository groupRoleRepository;
    private final GroupPermissionRepository groupPermissionRepository;

    public void setGroupUser(List<GroupInfoDTO> groups, WorkspaceUser workspaceUser) {
        for (GroupInfoDTO groupInfo : groups) {
            //1. group_user insert
            Group group = this.groupRepository.findByName(groupInfo.getGroupName());//그룹명을 가지고 그룹을 가져와서, group_user에 save 한다.

            if (group == null) {
                //존재하는 그룹이 없는 에러
                throw new BusinessException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
            }

            GroupUser groupUser = GroupUser.builder().group(group).workspaceUser(workspaceUser).build();
            this.groupUserRepository.save(groupUser);

            //2. group_user_permission insert
            this.setGroupUserPermission(groupInfo.getManagerAssign(), groupInfo.getGroupPermissions(), groupUser);
        }


    }

    public void setGroupUserPermission(Boolean managerAssign, List<Long> groupPermissions, GroupUser groupUser) {
        Long[] defaultPermissions = {2L, 3L, 4L};

        //그룹의 매니저로 권한 부여.
        GroupRole groupRole;
        GroupPermission groupPermission = null;
        if (managerAssign) {
            groupRole = this.groupRoleRepository.findByRole("MANAGER");
            if (groupPermissions != null) {
                //그룹의 매니저로, 권한을 개별적으로 선택했을때

                for (long permission : groupPermissions) {
                    groupPermission = this.groupPermissionRepository.findById(permission).get();
                    GroupUserPermission groupUserPermission = GroupUserPermission.builder()
                            .groupRole(groupRole)
                            .groupUser(groupUser)
                            .groupPermission(groupPermission)
                            .build();
                    this.groupUserPermissionRepository.save(groupUserPermission);
                }
            } else {
                //그룹의 매니저로, 권한을 선택하지 않았을때 : 매니저의 기본값(manager->234)
                for (long permission : defaultPermissions) {
                    groupPermission = this.groupPermissionRepository.findById(permission).get();
                    GroupUserPermission groupUserPermission = GroupUserPermission.builder()
                            .groupRole(this.groupRoleRepository.findByRole("MANAGER"))
                            .groupUser(groupUser)
                            .groupPermission(groupPermission)
                            .build();
                    this.groupUserPermissionRepository.save(groupUserPermission);
                }
            }
        } else {
            //그룹의 매니저가 아닌 멤버로
            groupRole = this.groupRoleRepository.findByRole("MEMBER");
            groupPermission = this.groupPermissionRepository.findById(5L).get();
            GroupUserPermission groupUserPermission = GroupUserPermission.builder()
                    .groupRole(groupRole)
                    .groupUser(groupUser)
                    .groupPermission(groupPermission)
                    .build();
            this.groupUserPermissionRepository.save(groupUserPermission);
        }
        log.info("[사용자 - " + groupUser.getWorkspaceUser().getUserId() + " ] [그룹명 - " + groupUser.getGroup().getName() + " ]  [직책 - " + groupRole.getRole() + " ] [권한 - " + groupPermission.getPermission() + " ]");

    }
/*
    public void reviseGroupUserPermission(List<GroupInfoDTO> groups, WorkspaceUser workspaceUser) {
        for (GroupInfoDTO groupInfoDTO : groups) {
            Group group = this.groupRepository.findByName(groupInfoDTO.getGroupName());

            if (group == null) {
                //존재하는 그룹이 없는 에러
                throw new BusinessException(ErrorCode.ERR_UNEXPECTED_SERVER_ERROR);
            }

            GroupUser groupUser = this.groupUserRepository.findByWorkspaceUserAndGroup(workspaceUser, group);
            //1. 기존 존재하던 권한들 삭제
            this.groupUserPermissionRepository.deleteAllByGroupUser(groupUser);
            //2. 새로 권한 부여
            this.setGroupUserPermission(groupInfoDTO.getManagerAssign(), groupInfoDTO.getGroupPermissions(), groupUser);
        }
    }*/
}
