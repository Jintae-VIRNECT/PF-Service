package com.virnect.license.application.rest.billing;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.billing.BillingProductInfoList;
import com.virnect.license.dto.rest.billing.BillingRestResponse;
import com.virnect.license.dto.rest.billing.MonthlyBillingCancelRequest;
import com.virnect.license.dto.rest.billing.MonthlyBillingInfo;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayAPIService {
	private static final String BILLING_USER_MONTHLY_BILL_INFO_URL = "/billing/user/monthbillinfo";
	private static final String BILLING_USER_MONTHLY_BILL_CANCEL_URL = "/billing/user/monthpaycnl";
	private static final String BILLING_PRODUCT_LIST_URL = "billing/product/list";
	private static final String BILLING_SITE_CODE_VARIABLE_NAME = "sitecode";
	private static final long BILLING_SITE_CODE_VALUE = 1L;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	@Value("${infra.billing.api:none}")
	private String billingApi;

	/**
	 * 정기 결제 취소 절차
	 * @param userNumber - 정기 결제 진행중인 사용자 식별자
	 */
	public void billingCancelProcess(long userNumber) {
		BillingRestResponse<MonthlyBillingInfo> userMonthlyBillingInfo = getUserMonthlyBillingInfo(userNumber);

		if(!userMonthlyBillingInfo.getData().getPaymentFlag().equals("Y")){
			log.info("User not have monthly billing information");
			return;
		}

		cancelUserMonthlyBilling(userMonthlyBillingInfo.getData(),userNumber);
	}

	/**
	 * 정기 결제 취소 요청 전송
	 * @param monthlyBillingInfo - 사용자의 정기결제 정보
	 * @param userId - 사용자 식별자
	 */
	public void cancelUserMonthlyBilling(MonthlyBillingInfo monthlyBillingInfo, long userId) {
		if (billingApi.equals("none") || monthlyBillingInfo == null) {
			log.info("BILLING API INFORMATION NOT INITIALIZED.");
			return;
		}

		MonthlyBillingCancelRequest cancelRequest = new MonthlyBillingCancelRequest();
		cancelRequest.setSiteCode(BILLING_SITE_CODE_VALUE);
		cancelRequest.setUserMonthlyBillingNumber(monthlyBillingInfo.getMonthlyBillingNumber());
		cancelRequest.setUserNumber(userId);

		try {
			URI uri = UriComponentsBuilder
				.fromUriString(billingApi)
				.path(BILLING_USER_MONTHLY_BILL_CANCEL_URL)
				.build()
				.toUri();


			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(cancelRequest), httpHeaders);


			// 정기 결제 취소
			BillingRestResponse<Map<String, Object>> billingCancelResult = restTemplate.exchange(
				uri, HttpMethod.POST, entity, BillingRestResponse.class)
				.getBody();

			// 정기 결제 취소 시, 페이레터 서버 에러인 경우
			if (billingCancelResult == null || billingCancelResult.getResult().getCode() != 0) {
				log.error("[BILLING_PAYLETTER] => Paylleter Server Error!");
				log.error("[BILLING_MONTHLY_BILLING_CANCEL] -> [{}]", cancelRequest.toString());
				throw new LicenseServiceException(ErrorCode.ERR_BILLING_MONTHLY_BILLING_CANCEL);
			}

			log.info("[BILLING_MONTHLY_BILLING_CANCEL_RESULT] -> [{}]", billingCancelResult.toString());
		} catch (Exception e) {
			log.error("[BILLING_MONTHLY_BILLING_CANCEL]", e);
			log.error("[BILLING_MONTHLY_BILLING_CANCEL] -> [{}]", cancelRequest.toString());
		}
	}

	/**
	 * 사용자의 정기 결제 내역 정보 조회
	 * @param userId - 사용자 식별자
	 * @return - 정기 결제 내역 정보 
	 */
	public BillingRestResponse<MonthlyBillingInfo> getUserMonthlyBillingInfo(long userId) {
		if (billingApi.equals("none")) {
			log.info("BILLING API INFORMATION NOT INITIALIZED.");
			return null;
		}
		// 사용자의 정기 결제 내역 정보 조회
		URI uri = UriComponentsBuilder
			.fromUriString(billingApi)
			.path(BILLING_USER_MONTHLY_BILL_INFO_URL)
			.queryParam(BILLING_SITE_CODE_VARIABLE_NAME, BILLING_SITE_CODE_VALUE)
			.queryParam("userno", userId)
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
			log.error("[BILLLING_MONTHLY_BILLING_INFO] -> [{}]", userId);
			throw new LicenseServiceException(ErrorCode.ERR_BILLING_MONTHLY_BILLING_INFO);
		}
		log.info("[BILLING_USER_MOHTLY_BILLING_INFO] -> [{}]", userMonthlyBillingInfo.toString());
		return userMonthlyBillingInfo;
	}

	/**
	 * 구매 가능한 전체 상품 정보 조회
	 * @return - 전체 상품 정보
	 */
	public BillingRestResponse<BillingProductInfoList> getBillingProductInfoList() {
		if (billingApi.equals("none")) {
			log.info("BILLING API INFORMATION NOT INITIALIZED.");
			return null;
		}

		URI uri = UriComponentsBuilder
			.fromUriString(billingApi)
			.path(BILLING_PRODUCT_LIST_URL)
			.queryParam(BILLING_SITE_CODE_VARIABLE_NAME, BILLING_SITE_CODE_VALUE)
			.build()
			.toUri();

		log.info("[PAY_API][GET_BILLING_PRODUCT_INFO_LIST][REQUEST] - {}", uri.toString());

		BillingRestResponse<BillingProductInfoList> billingProductInfoListRestResponse = restTemplate.exchange(
			uri, HttpMethod.GET, null,
			new ParameterizedTypeReference<BillingRestResponse<BillingProductInfoList>>() {
			}
		).getBody();

		if (billingProductInfoListRestResponse == null) {
			log.error(
				"[PAY_API][GET_BILLING_PRODUCT_INFO_LIST][RESPONSE_ERROR] - Receive Empty Response from Pay API Server");
			return null;
		}

		log.info(
			"[PAY_API][GET_BILLING_PRODUCT_INFO_LIST][RESPONSE] - {}", billingProductInfoListRestResponse.toString());

		return billingProductInfoListRestResponse;
	}
}
