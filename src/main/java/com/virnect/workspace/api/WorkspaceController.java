package com.virnect.workspace.api;

import com.virnect.workspace.application.WorkspaceService;
import com.virnect.workspace.dto.WorkspaceInfoDTO;
import com.virnect.workspace.dto.WorkspaceNewMemberInfoDTO;
import com.virnect.workspace.dto.request.WorkspaceCreateRequest;
import com.virnect.workspace.dto.request.WorkspaceUpdateRequest;
import com.virnect.workspace.dto.response.MemberListResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoListResponse;
import com.virnect.workspace.dto.response.WorkspaceInfoResponse;
import com.virnect.workspace.exception.WorkspaceException;
import com.virnect.workspace.global.common.ApiResponse;
import com.virnect.workspace.global.common.PageRequest;
import com.virnect.workspace.global.error.ErrorCode;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * Project: PF-Workspace
 * DATE: 2020-01-08
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION: workspace service rest api controller
 */
@Slf4j
@RestController
@RequestMapping("/workspaces")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @ApiOperation(
            value = "워크스페이스 시작하기",
            notes = "워크스페이스를 생성하는 기능입니다."
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = " 유저 uuid", dataType = "string", paramType = "form", defaultValue = "uuid", required = true),
            @ApiImplicitParam(name = "name", value = "워크스페이스 이름(빈값 일 경우 닉네임's Workspace로 저장됩니다.)", dataType = "string", paramType = "form", defaultValue = "USER's Workspace", required = true),
            @ApiImplicitParam(name = "profile", value = "워크스페이스 프로필", dataType = "__file", paramType = "form"),
            @ApiImplicitParam(name = "description", value = "워크스페이스 설명", dataType = "string", paramType = "form", defaultValue = "워크스페이스 입니다.", required = true)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceInfoDTO>> createWorkspace(@ModelAttribute @Valid WorkspaceCreateRequest workspaceCreateRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<WorkspaceInfoDTO> apiResponse = this.workspaceService.createWorkspace(workspaceCreateRequest);
        return ResponseEntity.ok(apiResponse);
    }
    @ApiOperation(
            value = "워크스페이스 프로필 설정",
            notes = "생성된 워크스페이스의 프로필을 변경하는 기능입니다.(마스터 유저만 가능한 기능입니다.)"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "마스터 유저 uuid", dataType = "string", paramType = "form", defaultValue = "uuid", required = true),
            @ApiImplicitParam(name = "name", value = "워크스페이스 이름(빈값 일 경우 닉네임's Workspace로 저장됩니다.)", dataType = "string", paramType = "form", defaultValue = "USER's Workspace", required = true),
            @ApiImplicitParam(name = "profile", value = "워크스페이스 프로필", dataType = "__file", paramType = "form"),
            @ApiImplicitParam(name = "description", value = "워크스페이스 설명", dataType = "string", paramType = "form", defaultValue = "워크스페이스 입니다.", required = true)
    })
    @PutMapping("/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceInfoDTO>> setWorkspace(@PathVariable("workspaceId") String workspaceId, @ModelAttribute @Valid WorkspaceUpdateRequest workspaceUpdateRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<WorkspaceInfoDTO> apiResponse = this.workspaceService.setWorkspace(workspaceId,workspaceUpdateRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @ApiOperation(
            value = "워크스페이스 이미지 조회(개발 서버 업로드)"
    )
    @ApiImplicitParam(name = "fileName", value = "파일 이름", dataType = "string", type = "path", defaultValue = "1.PNG", required = true)
    @GetMapping("/upload/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        if (!StringUtils.hasText(fileName)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        Resource resource = this.workspaceService.downloadFile(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @ApiOperation(
            value = "내가 속한 워크스페이스 목록 조회",
            notes = "사용자가 마스터, 매니저, 멤버로 소속되어 있는 워크스페이스 정보를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<WorkspaceInfoListResponse>> getUserWorkspaces(@RequestParam("userId") String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<WorkspaceInfoListResponse> apiResponse = this.workspaceService.getUserWorkspaces(userId);
        return ResponseEntity.ok(apiResponse);
    }

    @ApiOperation(
            value = "워크스페이스 멤버 검색(워크스페이스 멤버 목록 조회)",
            notes = "워크스페이스 멤버 검색으로 멤버를 조회합니다."
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "search", value = "검색어(닉네임, 이메일)", dataType = "string", allowEmptyValue = true, defaultValue = ""),
            @ApiImplicitParam(name = "filter", value = "사용자 필터(MASTER, MANAGER, MEMBER)", dataType = "string", allowEmptyValue = true, defaultValue = ""),
            @ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호", paramType = "query", defaultValue = "0"),
            @ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "20"),
            @ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "email,desc"),
    })
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<ApiResponse<MemberListResponse>> getMembers(@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId, @RequestParam(value = "search", required = false) String search, @RequestParam(value = "filter", required = false) String filter, @ApiIgnore PageRequest pageable) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(workspaceId)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<MemberListResponse> apiResponse = this.workspaceService.getMembers(workspaceId, userId, search, filter, pageable);
        return ResponseEntity.ok(apiResponse);
    }



    @ApiOperation(
            value = "워크스페이스 홈 - 정보 조회",
            notes = "워크스페이스 홈에서 워크스페이스의 정보를 조회합니다."
    )
    @GetMapping("/home/{workspaceId}")
    public ResponseEntity<ApiResponse<WorkspaceInfoResponse>> getWorkspaceInfo(@PathVariable("workspaceId") String workspaceId) {
        if (!StringUtils.hasText(workspaceId)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }
        ApiResponse<WorkspaceInfoResponse> apiResponse = this.workspaceService.getWorkspaceInfo(workspaceId);
        return ResponseEntity.ok(apiResponse);
    }

    @ApiOperation(
            value = "워크스페이스 홈 - 신규 멤버 조회",
            notes = "워크스페이스 홈에서 워크스페이스의 신규 참여 멤버 정보를 조회합니다."
    )
    @GetMapping("/home/{workspaceId}/members")
    public ResponseEntity<ApiResponse<List<WorkspaceNewMemberInfoDTO>>> getWorkspaceNewUserInfo(@PathVariable("workspaceId") String workspaceId) {
        if (!StringUtils.hasText(workspaceId)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }
        ApiResponse<List<WorkspaceNewMemberInfoDTO>> apiResponse = this.workspaceService.getWorkspaceNewUserInfo(workspaceId);
        return ResponseEntity.ok(apiResponse);
    }

 /*
    @ApiOperation(
            value = "워크스페이스 멤버 권한 설정",
            notes = "워크스페이스 사용자에서 멤버의 권한, 플랜할당을 설정합니다."
    )
    @PostMapping("/{workspaceId}/members")
    public ResponseEntity<ApiResponse<Boolean>> reviseUserPermission(@PathVariable("workspaceId") String workspaceId, @RequestBody MembersRoleUpdateRequest membersRoleUpdateRequest, BindingResult bindingResult) {
        if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }
        ApiResponse<Boolean> apiResponse = this.workspaceService.reviseUserPermission(workspaceId, membersRoleUpdateRequest);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{workspaceId}/invite")
    public ResponseEntity<ApiResponse<WorkspaceInviteRestResponse>> inviteWorkspace(@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId, @RequestBody @Valid WorkspaceInviteRequest workspaceInviteRequestList) {
        if (!StringUtils.hasText(workspaceId)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }
        ApiResponse<WorkspaceInviteRestResponse> apiResponse = this.workspaceService.inviteWorkspace(workspaceId, userId, workspaceInviteRequestList);
        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/{workspaceId}/invite/accept")
    public ResponseEntity<ApiResponse<WorkspaceInviteAcceptResponse>> inviteWorkspaceAccept(@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId, @RequestParam("code") String code) {
        if (!StringUtils.hasText(workspaceId)) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }
        ApiResponse<WorkspaceInviteAcceptResponse> apiResponse = this.workspaceService.inviteWorkspaceAccept(workspaceId, userId, code);
        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/{workspaceId}/create")
    public ResponseEntity<ApiResponse<UsersCreateResponse>> createUsers(@PathVariable("workspaceId") String workspaceId, @RequestParam("userId") String userId, @RequestBody @Valid UsersCreateRequest userCreateRequest, BindingResult bindingResult) {
        if (!StringUtils.hasText(workspaceId) || bindingResult.hasErrors()) {
            throw new WorkspaceException(ErrorCode.ERR_INVALID_VALUE);
        }
        ApiResponse<UsersCreateResponse> apiResponse = this.workspaceService.createUsers(workspaceId, userId, userCreateRequest);
        return ResponseEntity.ok(apiResponse);
    }
*/
}
