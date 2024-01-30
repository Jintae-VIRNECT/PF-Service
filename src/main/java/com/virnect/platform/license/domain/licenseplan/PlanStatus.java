package com.virnect.platform.license.domain.licenseplan;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */
public enum PlanStatus {
	ACTIVE, // 활성화
	INACTIVE, // 비활성화 (라이선스 갱신 기간 만료)
	TERMINATE // 종료 (라이서스 갱신 유예 기간 만료 또는 회원 탈퇴)
}
