package com.virnect.workspace.domain.group;

import com.virnect.workspace.domain.TimeEntity;
import lombok.*;

import javax.persistence.*;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-08
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
@Entity
@Getter
@Setter
@Table(name = "group_user_permission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupUserPermission extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_user_permission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_role_id")
    private GroupRole groupRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_permission_id")
    private GroupPermission groupPermission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_user_id")
    private GroupUser groupUser;

    @Builder
    public GroupUserPermission(GroupRole groupRole, GroupPermission groupPermission, GroupUser groupUser){
        this.groupRole = groupRole;
        this.groupPermission = groupPermission;
        this.groupUser = groupUser;
    }
}
