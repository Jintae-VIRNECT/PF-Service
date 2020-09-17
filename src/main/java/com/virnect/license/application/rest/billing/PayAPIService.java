package com.virnect.license.application.rest.billing;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.billing.BillingRestResponse;
import com.virnect.license.dto.rest.billing.MonthlyBillingCancelRequest;
import com.virnect.license.dto.rest.billing.MonthlyBillingInfo;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayAPIService {
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${infra.billing.api}")
	private String billingApi;

	/**
	 * 정기 결제 취소 처리
	 * @param userNumber - 정기 결제 진행중인 사용자 식별자
	 */
	public void billingCancelProcess(long userNumber) {
		// 사용자의 정기 결제 내역 정보 조회
		URI uri = UriComponentsBuilder
			.fromUriString(billingApi)
			.path("/billing/user/monthbillinfo")
			.queryParam("sitecode", 1)
			.queryParam("userno", userNumber)
			.build()
			.toUri();

		BillingRestResponse<MonthlyBillingInfo> userMonthlyBillingInfo = restTemplate.exchange(
			uri, HttpMethod.GET, null, new ParameterizedTypeReference<BillingRestResponse<MonthlyBillingInfo>>() {
			}).getBody();

		// 정기 결제 내역 조회 시, 페이레터 서버 에러인 경우
		if (userMonthlyBillingInfo == null || userMonthlyBillingInfo.getData() == null
			|| userMonthlyBillingInfo.getResult().getCode() != 0
		) {
			log.error("[BILLING_PAYLETTER] => Paylleter Server Error!");
			log.error("[BILLLING_MONTHLY_BILLING_INFO] -> [{}]", userNumber);
			throw new LicenseServiceException(ErrorCode.ERR_BILLING_MONTHLY_BILLING_INFO);
		}

		MonthlyBillingInfo monthlyBillingInfo = userMonthlyBillingInfo.getData();

		log.info("[BILLING_USER_MOHTLY_BILLING_INFO] -> [{}]", monthlyBillingInfo.toString());

		// payment flag Y= 정기결제 이용중, N: 해지 상태, D: 등록된 정기 결제 내용 없음
		if (monthlyBillingInfo.getPaymentFlag().equals("Y")) {
			MonthlyBillingCancelRequest cancelRequest = new MonthlyBillingCancelRequest();
			cancelRequest.setSiteCode(1);
			cancelRequest.setUserMonthlyBillingNumber(monthlyBillingInfo.getMonthlyBillingNumber());
			cancelRequest.setUserNumber(userNumber);
			try {
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(cancelRequest), httpHeaders);

				// 정기 결제 취소
				BillingRestResponse<Map<String, Object>> billingCancelResult = restTemplate.exchange(
					billingApi + "/billing/user/monthpaycnl", HttpMethod.POST, entity, BillingRestResponse.class).getBody();

				// 정기 결제 취소 시, 페이레터 서버 에러인 경우
				if (billingCancelResult == null || billingCancelResult.getResult().getCode() != 0) {
					log.error("[BILLING_PAYLETTER] => Paylleter Server Error!");
					log.error("[BILLING_MONTHLY_BILLING_CANCEL] -> [{}]", cancelRequest.toString());
					throw new LicenseServiceException(ErrorCode.ERR_BILLING_MONTHLY_BILLING_CANCEL);
				}

				log.info(billingCancelResult.toString());
			} catch (Exception e) {
				log.error("[BILLING_MONTHLY_BILLING_CANCEL]", e);
				log.error("[BILLING_MONTHLY_BILLING_CANCEL] -> [{}]", cancelRequest.toString());
			}
		}
	}
}
