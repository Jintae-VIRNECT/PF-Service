package com.virnect.license.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserLicenseDetailsInfo {
    private final String workspaceId;
    private final String productName;
    private final LocalDateTime endDate;

    @Override
    public String toString() {
        return "UserLicenseDetailsInfo{" +
                "workspaceId='" + workspaceId + '\'' +
                ", productName='" + productName + '\'' +
                ", endDate=" + endDate +
                '}';
    }
}
