package com.virnect.license.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dao.applicense.AppLicenseRepository;
import com.virnect.license.domain.applicense.AppLicense;
import com.virnect.license.domain.applicense.AppLicenseStatus;
import com.virnect.license.dto.sdk.request.SDKLicenseRegisterRequest;
import com.virnect.license.dto.sdk.response.SDKLicenseAuthenticationResponse;
import com.virnect.license.dto.sdk.response.SDKLicenseInfoListResponse;
import com.virnect.license.dto.sdk.response.SDKLicenseInfoResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.PageMetadataResponse;
import com.virnect.license.global.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class SDKLicenseService {
	private static final int LICENSE_EXPIRED_HOUR = 23; // 오후 11시
	private static final int LICENSE_EXPIRED_MINUTE = 59; // 59분
	private static final int LICENSE_EXPIRED_SECONDS = 59; // 59초
	private final AppLicenseRepository appLicenseRepository;
	private final ModelMapper modelMapper;

	@Transactional
	public SDKLicenseInfoResponse registerNewSdkLicense(SDKLicenseRegisterRequest licenseRegisterRequest) {
		log.info("[SDK_LICENSE_REGISTER][REQUEST] - {}", licenseRegisterRequest.toString());

		boolean isRegisteredSerialKey = appLicenseRepository.existsBySerialKey(licenseRegisterRequest.getSerialKey());
		if (isRegisteredSerialKey) {
			log.error("[SDK_LICENSE_REGISTER][ALREADY_EXIST] - {}", licenseRegisterRequest.toString());
			throw new LicenseServiceException(ErrorCode.ERR_SDK_LICENSE_DUPLICATE);
		}

		LocalDateTime startDate;
		LocalDateTime expiredDate;
		try {
			startDate = LocalDate.parse(licenseRegisterRequest.getStartDate()).atTime(LocalTime.now());
			expiredDate = LocalDate.parse(licenseRegisterRequest.getExpiredDate())
				.atTime(LICENSE_EXPIRED_HOUR, LICENSE_EXPIRED_MINUTE, LICENSE_EXPIRED_SECONDS);
		} catch (DateTimeParseException e) {
			log.error("[SDK_LICENSE_DATE_PARSE_ERROR] - {}", licenseRegisterRequest.toString());
			log.error(e.getMessage(), e);
			throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}

		AppLicense appLicense = new AppLicense();
		appLicense.setSerialKey(licenseRegisterRequest.getSerialKey());
		appLicense.setStartDate(startDate);
		appLicense.setExpiredDate(expiredDate);
		appLicense.setStatus(AppLicenseStatus.UNUSED);
		appLicense.setHit(0L);

		log.info("[SDK_LICENSE_REGISTER][GENERATE_NEW_LICENSE] - {}", appLicense.toString());
		appLicenseRepository.save(appLicense);

		return modelMapper.map(appLicense, SDKLicenseInfoResponse.class);
	}

	@Transactional
	public SDKLicenseAuthenticationResponse validateSerialKey(String serialKey) {
		Optional<AppLicense> targetLicense = appLicenseRepository.findBySerialKey(serialKey);

		if (!targetLicense.isPresent()) {
			log.error("[SDK_LICENSE_VALIDATION][SERIAL_KEY][NOT_FOUND] - [{}]", serialKey);
			throw new LicenseServiceException(ErrorCode.ERR_SDK_LICENSE_NOT_FOND);
		}

		AppLicense appLicense = targetLicense.get();

		// Check License period expiration
		if (appLicense.getExpiredDate().isBefore(LocalDateTime.now()) ||
			appLicense.getStatus().equals(AppLicenseStatus.TERMINATE)
		) {

			log.info("[SDK_LICENSE_PERIOD_EXPIRED] - CURRENT_DATE: {}, LICENSE_EXPIRED_DATE: {}",
				LocalDateTime.now(), appLicense.getExpiredDate()
			);
			log.info("[SDK_LICENSE_PERIOD_EXPIRED] - {}", appLicense.toString());

			if (!appLicense.getStatus().equals(AppLicenseStatus.TERMINATE)) {
				appLicense.setStatus(AppLicenseStatus.TERMINATE);
				appLicenseRepository.save(appLicense);
			}

			throw new LicenseServiceException(ErrorCode.ERR_SDK_LICENSE_TERMINATE);
		}

		// Change app license status to use, if current status unused
		if (appLicense.getStatus().equals(AppLicenseStatus.UNUSED)) {
			appLicense.setStatus(AppLicenseStatus.USE);
			appLicenseRepository.save(appLicense);
		}

		// increase hit of authentication request on current license
		appLicense.increaseHit();

		// save
		appLicenseRepository.save(appLicense);

		SDKLicenseAuthenticationResponse licenseAuthenticationResponse = new SDKLicenseAuthenticationResponse();
		licenseAuthenticationResponse.setValidationResult(true);
		licenseAuthenticationResponse.setStartDate(appLicense.getStartDate());
		licenseAuthenticationResponse.setExpiredDate(appLicense.getExpiredDate());
		licenseAuthenticationResponse.setStatus(appLicense.getStatus());

		return licenseAuthenticationResponse;
	}

	@Transactional(readOnly = true)
	public SDKLicenseInfoListResponse getLicenseInfoList(String search, Pageable pageable) {
		Page<AppLicense> appLicenseList;
		if (search.equals("ALL")) {
			appLicenseList = appLicenseRepository.findAll(pageable);
		} else {
			appLicenseList = appLicenseRepository.findAllBySerialKeyContaining(search, pageable);
		}
		List<SDKLicenseInfoResponse> appLicenseInfoListResponse = appLicenseList.stream()
			.map(appLicense -> modelMapper.map(appLicense, SDKLicenseInfoResponse.class))
			.collect(Collectors.toList());

		PageMetadataResponse pageMetadataResponse = PageMetadataResponse.builder()
			.currentPage(pageable.getPageNumber())
			.currentSize(pageable.getPageSize())
			.totalPage(appLicenseList.getTotalPages())
			.totalElements(appLicenseList.getTotalElements())
			.build();

		return new SDKLicenseInfoListResponse(appLicenseInfoListResponse, pageMetadataResponse);
	}
}
