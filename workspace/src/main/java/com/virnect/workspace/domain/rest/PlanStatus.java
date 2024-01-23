package com.virnect.workspace.domain.rest;

/**
 * Project: PF-Workspace
 * DATE: 2020-10-07
 * AUTHOR: jkleee (Jukyoung Lee)
 * EMAIL: ljk@virnect.com
 * DESCRIPTION:
 */
public enum PlanStatus {
	ACTIVE, // 활성화
	INACTIVE, // 비활성화 (라이선스 갱신 기간 만료)
	TERMINATE // 종료 (라이서스 갱신 유예 기간 만료 또는 회원 탈퇴)
}
