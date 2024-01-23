package com.virnect.workspace.global.error;

/**
 * Project: base
 * DATE: 2020-01-07
 * AUTHOR: JohnMark (Chang Jeong Hyeon)
 * EMAIL: practice1356@gmail.com
 * DESCRIPTION:
 */
public enum ErrorCode {
	// 워크스페이스 에러
	ERR_MASTER_WORKSPACE_ALREADY_EXIST(1001, "User already have master workspace."),
	ERR_WORKSPACE_USER_ALREADY_EXIST(1002, "Workspace User already exist."),
	ERR_WORKSPACE_INVALID_PERMISSION(1003, "Invalid permission to the Workspace."),

	//워크스페이스 초대 관련 에러
	ERR_NOT_FOUND_INVITE_WORKSPACE_INFO(1004, "Not found Workspace Invite Code."),
	ERR_INCORRECT_INVITE_WORKSPACE_CODE(1005, "Incorrect Workspace Invite Code."),
	ERR_WORKSPACE_INVITE(1006, "Workspace Invite Fail."),
	ERR_WORKSPACE_INVITE_MAX_USER(1016, "Workspace invite fail. Because over maximum number of user participation."),
	ERR_WORKSPACE_INVITE_MAX_JOIN_USER(1017, "Workspace invite fail. Because over maximum number of workspace join."),
	ERR_WORKSPACE_INVITE_SECESSION_USER(1018, "Workspace invite fail. Because invited user is secession user."),
	ERR_WORKSPACE_INVITE_NON_LICENSE(1019, "Workspace invite fail. Because non grantable license."),

	//워크스페이스 관련
	ERR_INCORRECT_USER_LICENSE_INFO(1007, "Users must have at least 1 plan."),
	ERR_WORKSPACE_MAX_USER_AMOUNT_OVER(1008, "Workspace max user mount is over."),
	ERR_WORKSPACE_NOT_FOUND(1009, "Workspace not found"),
	ERR_WORKSPACE_USER_NOT_FOUND(1010, "Workspace User not found"),
	ERR_WORKSPACE_USER_ACCOUNT_CREATE_FAIL(1011, "Workspace user account create fail"),
	ERR_WORKSPACE_USER_ACCOUNT_DELETE_FAIL(1012, "Workspace user account delete fail"),
	ERR_WORKSPACE_USER_PASSWORD_CHANGE(1013, "Workspace user password change fail. Workspace Member Type User Not found"),
	ERR_WORKSPACE_ROLE_NOT_FOUND(1014, "Workspace Role not found"),
	ERR_WORKSPACE_PERMISSION_NOT_FOUND(1015, "Workspace Permission not found"),
	ERR_WORKSPACE_USER_ACCOUNT_DELETE_ACCESS(1016, "Workspace user account delete fail. Because invalid access information."),

	//워크스페이스 유저 정보 수정 에러
	ERR_WORKSPACE_USER_INFO_UPDATE(1020, "Workspace user info update fail."),
	ERR_WORKSPACE_USER_INFO_UPDATE_MASTER_PLAN(1021, "Workspace user info update fail. Master user plan is only available for master user."),
	ERR_WORKSPACE_USER_INFO_UPDATE_USER_TYPE(1022, "Workspace user info update fail. User type that can not be update information."),

	//워크스페이스 게스트 계정 관련 에러
	ERR_WORKSPACE_GUEST_USER_CREATE(1030, "Workspace guest user create fail."),
	ERR_WORKSPACE_GUEST_USER_CREATE_LACK_LICENSE(1031, "Workspace guest user create fail. Workspace license plan is lack"),
	ERR_WORKSPACE_GUEST_USER_DELETE(1032, "Workspace guest user delete fail."),

	//워크스페이스 생성 에러
	ERR_WORKSPACE_CREATE(1100, "Workspace Create fail."),
	ERR_WORKSPACE_CREATE_INVALID_PROFILE(1101, "Workspace create fail. Because invalid workspace profile image."),
	ERR_WORKSPACE_CREATE_MAX_CREATE(1102, "Workspace create fail. Because over maximum number of workspace creation."),

	//워크스페이스 설정 관련 에러
	ERR_WORKSPACE_SETTING(1200, "Workspace setting fail. Please Contact Administrator."),
	ERR_WORKSPACE_SETTING_NOT_FOUND(1201, "Workspace setting fail. Not found setting info."),
	ERR_WORKSPACE_SETTING_VALUE_INVALID(1202, "Workspace setting fail. Invalid setting value."),

	//라이선스 관련 에러
	ERR_WORKSPACE_USER_LICENSE_GRANT_FAIL(2000, "Workspace user license grant fail"),
	ERR_WORKSPACE_USER_LICENSE_REVOKE_FAIL(2001, "Workspace user license revoke fail"),
	ERR_NOT_FOUND_WORKSPACE_LICENSE_PLAN(2002, "Workspace license plan not found"),
	ERR_NOT_FOUND_USEFUL_WORKSPACE_LICENSE(2003, "Useful workspace license not found"),

	//파일 관련 에러
	ERR_NOT_ALLOW_FILE_EXTENSION(3000, "Not Allow file extension."),
	ERR_NOT_ALLOW_FILE_SIZE(3001, "Not Allow file size."),
	ERR_NOT_FOUND_FILE(3002, "Not Found file."),

	// 공통 에러
	ERR_API_AUTHENTICATION(8003, "Authentication Error"),

	ERR_INVALID_VALUE(8004, "Invalid Value"),

	ERR_INVALID_REQUEST_PARAMETER(8001, "Invalid request parameter cause api errors"),

	ERR_UNEXPECTED_SERVER_ERROR(9999, "Unexpected Server Error, Please contact Administrator");

	private final int code;
	private final String message;

	ErrorCode(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return new ErrorResponseMessage(this).toString();
	}
}
