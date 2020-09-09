package com.virnect.license.application.rest.billing;

import java.util.Map;

import org.springframework.stereotype.Component;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dto.rest.billing.BillingRestResponse;
import com.virnect.license.dto.rest.billing.MonthlyBillingCancelRequest;
import com.virnect.license.dto.rest.billing.MonthlyBillingInfo;

@Slf4j
@Component
public class BillingRestFallbackFactory implements FallbackFactory<BillingRestService> {
	@Override
	public BillingRestService create(Throwable cause) {
		return new BillingRestService() {
			@Override
			public BillingRestResponse<MonthlyBillingInfo> getMonthlyBillingInfo(int siteCode, long userNumber) {
				log.error(
					"[BILLING_PAYLETTER_ERROR][GET :: getMonthlyBillingInfo] -> \\{ sitecode: {} , userno: {} \\}",
					siteCode, userNumber
				);
				log.error(cause.getMessage(), cause);
				return null;
			}

			@Override
			public BillingRestResponse<Map<String, Object>> monthlyBillingCancel(
				MonthlyBillingCancelRequest monthlyBillingCancelRequest
			) {
				log.error(
					"[BILLING_PAYLETTER_ERROR][POST :: monthlyBillingCancel] -> [{}]",
					monthlyBillingCancelRequest.toString()
				);
				log.error(cause.getMessage(), cause);
				return null;
			}

			@Override
			public BillingRestResponse<Map<String, Object>> monthlyBillingCancelRollback(
				MonthlyBillingCancelRequest monthlyBillingCancelRollbackRequest
			) {
				log.error(
					"[BILLING_PAYLETTER_ERROR][POST :: monthlyBillingCancelRollback] -> [{}]",
					monthlyBillingCancelRollbackRequest.toString()
				);
				log.error(cause.getMessage(), cause);
				return null;
			}
		};
	}
}
