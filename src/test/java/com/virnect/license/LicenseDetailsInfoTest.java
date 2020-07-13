package com.virnect.license;

import com.virnect.license.dao.license.LicenseRepository;
import com.virnect.license.dto.UserLicenseDetailsInfo;
import com.virnect.license.global.common.PageRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("local")
public class LicenseDetailsInfoTest {
    @Autowired
    LicenseRepository licenseRepository;

    @Test
    @Transactional
    public void getMyLicenseDetailInfo() {
        PageRequest pageRequest = new PageRequest();
        List<UserLicenseDetailsInfo> detailsInfos = licenseRepository.findAllMyLicenseInfo("498b1839dc29ed7bb2ee90ad6985c608", pageRequest.of()).getContent();

        detailsInfos.stream()
                .forEach( d -> log.info(d.toString()));
    }
}