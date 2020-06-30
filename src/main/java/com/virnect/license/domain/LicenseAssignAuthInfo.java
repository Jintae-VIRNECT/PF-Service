package com.virnect.license.domain;

import com.virnect.license.dto.request.LicenseAllocateProductInfoResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@RedisHash("LicenseAssignAuthInfo")
public class LicenseAssignAuthInfo {
    @Id
    private String assignAuthCode;
    private Long userId;
    private String uuid;
    private String userName;
    private String email;
    private List<LicenseAllocateProductInfoResponse> productInfoList;
    private LocalDateTime assignableCheckDate;
    private Long totalProductCallTime;
    private Long totalProductHit;
    private Long totalProductStorage;
    @TimeToLive
    private Long expiredDate;

    @Override
    public String toString() {
        return "LicenseAssignAuthInfo{" +
                "assignAuthCode='" + assignAuthCode + '\'' +
                ", userId=" + userId +
                ", uuid='" + uuid + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", productInfoList=" + productInfoList +
                ", assignableCheckDate=" + assignableCheckDate +
                ", totalProductCallTime=" + totalProductCallTime +
                ", totalProductHit=" + totalProductHit +
                ", totalProductStorage=" + totalProductStorage +
                ", expiredDate=" + expiredDate +
                '}';
    }
}
