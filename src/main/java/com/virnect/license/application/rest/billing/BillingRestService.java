package com.virnect.license.application.rest.billing;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.virnect.license.dto.rest.billing.BillingRestResponse;
import com.virnect.license.dto.rest.billing.MonthlyBillingCancelRequest;
import com.virnect.license.dto.rest.billing.MonthlyBillingInfo;

@FeignClient(value = "${infra.billing.api}")
public interface BillingRestService {
	@GetMapping("/billing/user/monthbillinfo")
	BillingRestResponse<MonthlyBillingInfo> getMonthlyBillingInfo(
		@RequestParam(value = "sitecode", defaultValue = "1") int siteCode,
		@RequestParam(value = "userno") long userNumber
	);

	@PostMapping("/billing/user/monthpaycnl")
	BillingRestResponse<Map<String, Object>> monthlyBillingCancel(
		@RequestBody MonthlyBillingCancelRequest monthlyBillingCancelRequest
	);

	@PostMapping("/billing/user/monthpaycnl/reversal")
	BillingRestResponse<Map<String, Object>> monthlyBillingCancelRollback(
		@RequestBody MonthlyBillingCancelRequest monthlyBillingCancelRollbackRequest
	);
}
