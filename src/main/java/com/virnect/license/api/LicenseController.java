package com.virnect.license.api;

import com.virnect.license.application.LicenseService;
import com.virnect.license.dto.request.*;
import com.virnect.license.dto.response.*;
import com.virnect.license.dto.response.admin.AdminCouponInfoListResponse;
import com.virnect.license.dto.response.biling.ProductInfoListResponse;
import com.virnect.license.dto.response.biling.ProductInfoResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoListResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.common.PageRequest;
import com.virnect.license.global.error.ErrorCode;
import com.virnect.license.global.middleware.EncodingRequestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * @author jeonghyeon.chang (johnmark)
 * @project PF-License
 * @email practice1356@gmail.com
 * @description
 * @since 2020.04.09
 */

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class LicenseController {
    private final LicenseService licenseService;

    @ApiOperation(value = "이벤트 쿠폰 발급 요청")
    @PostMapping("/event/coupon")
    public ResponseEntity<ApiResponse<EventCouponResponse>> requestEventCouponRequestHandler(@RequestBody @Valid EventCouponRequest eventCouponRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<EventCouponResponse> responseMessage = this.licenseService.generateEventCoupon(eventCouponRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "쿠폰 등록")
    @PostMapping("/coupon/register")
    public ResponseEntity<ApiResponse<MyCouponInfoResponse>> couponRegisterRequestHandler(@RequestBody @Valid CouponRegisterRequest couponRegisterRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<MyCouponInfoResponse> responseMessage = this.licenseService.couponRegister(couponRegisterRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "내 쿠폰 정보 리스트 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "검색어(이메일/사용자명)", dataType = "string", paramType = "path", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608"),
            @ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "2"),
            @ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호(0부터 시작)", paramType = "query", defaultValue = "0"),
            @ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "createdDate,desc"),
    })
    @GetMapping("/coupon/{userId}")
    public ResponseEntity<ApiResponse<MyCouponInfoListResponse>> getMyCouponInfoListRequestHandler(@PathVariable("userId") String userId, @ApiIgnore PageRequest pageable) {
        if (StringUtils.isEmpty(userId)) {
            log.info("[userId] is invalid");
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<MyCouponInfoListResponse> responseMessage = this.licenseService.getMyCouponInfoList(userId, pageable.of());
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "내 쿠폰 사용")
    @PutMapping("/coupon/active")
    public ResponseEntity<ApiResponse<MyCouponInfoResponse>> couponActiveRequestHandler(@RequestBody @Valid CouponActiveRequest couponActiveRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<MyCouponInfoResponse> responseMessage = this.licenseService.couponActiveHandler(couponActiveRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "전체 쿠폰 정보 조회", tags = "ADMIN")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "size", value = "페이징 사이즈", dataType = "number", paramType = "query", defaultValue = "2"),
            @ApiImplicitParam(name = "page", value = "size 대로 나눠진 페이지를 조회할 번호(0부터 시작)", paramType = "query", defaultValue = "0"),
            @ApiImplicitParam(name = "sort", value = "정렬 옵션 데이터", paramType = "query", defaultValue = "createdDate,desc"),
    })
    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<AdminCouponInfoListResponse>> getAllCouponInfoRequestHandler(@ApiIgnore PageRequest pageRequest) {
        ApiResponse<AdminCouponInfoListResponse> responseMessage = this.licenseService.getAllCouponInfo(pageRequest.of());
        return ResponseEntity.ok(responseMessage);
    }


    @ApiOperation(value = "워크스페이스 라이선스 플랜 정보 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8"),
    })
    @GetMapping("/{workspaceId}/plan")
    public ResponseEntity<ApiResponse<WorkspaceLicensePlanInfoResponse>> getWorkspaceLicensePlanInfo(@PathVariable("workspaceId") String workspaceId) {
        if (!StringUtils.hasText(workspaceId)) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<WorkspaceLicensePlanInfoResponse> responseMessage = this.licenseService.getWorkspaceLicensePlanInfo(workspaceId);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "워크스페이스에서 할당받은 내 라이선스 정보 조회")
    @GetMapping("/{workspaceId}/{userId}")
    public ResponseEntity<ApiResponse<MyLicenseInfoListResponse>> getMyLicenseInfoRequestHandler(@PathVariable("userId") @NotBlank String userId, @PathVariable("workspaceId")
    @NotBlank String workspaceId) {
        if (!StringUtils.hasText(userId)) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<MyLicenseInfoListResponse> responseMessage = this.licenseService.getMyLicenseInfoList(userId, workspaceId);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "워크스페이스내에서 라이선스 할당")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8", required = true),
            @ApiImplicitParam(name = "userId", value = "워크스페이스 유저 식별자", paramType = "path", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608", required = true),
            @ApiImplicitParam(name = "productName", value = "제품명", paramType = "query", defaultValue = "make", required = true),
    })
    @PutMapping("/{workspaceId}/{userId}/grant")
    public ResponseEntity<ApiResponse<MyLicenseInfoResponse>> grantWorkspaceLicenseToUser(
            @PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId, @RequestParam(value = "productName") String productName) {
        if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId) || !StringUtils.hasText(productName)) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<MyLicenseInfoResponse> responseMessage = this.licenseService.grantWorkspaceLicenseToUser(workspaceId, userId, productName, true);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "워크스페이스내에서 라이선스 할당 해제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "workspaceId", value = "워크스페이스 식별자", paramType = "path", defaultValue = "4d6eab0860969a50acbfa4599fbb5ae8"),
            @ApiImplicitParam(name = "userId", value = "워크스페이스 유저 식별자", paramType = "path", defaultValue = "498b1839dc29ed7bb2ee90ad6985c608"),
            @ApiImplicitParam(name = "productName", value = "제품명", paramType = "query", defaultValue = "make"),
    })
    @PutMapping("/{workspaceId}/{userId}/revoke")
    public ResponseEntity<ApiResponse<Boolean>> revokeWorkspaceLicenseToUser(
            @PathVariable("workspaceId") String workspaceId, @PathVariable("userId") String userId, @RequestParam(value = "productName") String productName) {
        if (!StringUtils.hasText(workspaceId) || !StringUtils.hasText(userId) || !StringUtils.hasText(productName)) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<Boolean> responseMessage = this.licenseService.grantWorkspaceLicenseToUser(workspaceId, userId, productName, false);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "신규 상품 등록", tags = "BillingSystem")
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductInfoListResponse>> createNewProductRequest(@RequestBody CreateNewProductRequest createNewProductRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<ProductInfoListResponse> responseMessage = this.licenseService.createNewProductHandler(createNewProductRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 정보 조회", tags = "BillingSystem")
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<ProductInfoListResponse>> getAllProductInfoRequest() {
        ApiResponse<ProductInfoListResponse> responseMessage = this.licenseService.getAllProductInfo();
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 정보 수정", tags = "BillingSystem")
    @PutMapping("/products")
    public ResponseEntity<ApiResponse<ProductInfoResponse>> changeProductInfoRequest(@RequestBody @Valid ProductInfoUpdateRequest productInfoUpdateRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<ProductInfoResponse> responseMessage = this.licenseService.updateProductInfo(productInfoUpdateRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 삭제", notes = "상품 데이터는 실제로 삭제되진 않으며, 목록에서 표출만 되지않는다", tags = "BillingSystem")
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductInfoListResponse>> deleteProductRequest(@PathVariable("productId") long productId) {
        if (productId <= 0) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<ProductInfoListResponse> responseMessage = this.licenseService.deleteProduct(productId);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 타입 등록", tags = "BillingSystem")
    @PostMapping("/products/types")
    public ResponseEntity<ApiResponse<ProductTypeInfoListResponse>> createNewProductTypeInfo(@RequestBody @Valid CreateNewProductTypeRequest createNewProductTypeRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<ProductTypeInfoListResponse> responseMessage = this.licenseService.createNewProductTypeHandler(createNewProductTypeRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 타입 정보 조회", tags = "BillingSystem")
    @GetMapping("/products/types")
    public ResponseEntity<ApiResponse<ProductTypeInfoListResponse>> getAllProductTypeInRequest() {
        ApiResponse<ProductTypeInfoListResponse> responseMessage = this.licenseService.getAllProductTypeInfo();
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 타입 정보 수정", tags = "BillingSystem")
    @PutMapping("/products/types")
    public ResponseEntity<ApiResponse<ProductTypeInfoListResponse>> updateProductTypeInfoRequest(@RequestBody @Valid ProductTypeUpdateRequest productTypeUpdateRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        ApiResponse<ProductTypeInfoListResponse> responseMessage = this.licenseService.updateProductTypeInfo(productTypeUpdateRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 지급", tags = "BillingSystem")
    @PostMapping("/allocate")
    public ResponseEntity<EncodingRequestResponse> licenseProductAllocateToUser(@RequestBody @Valid LicenseProductAllocateRequest licensePRoductAllocateRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        EncodingRequestResponse responseMessage = this.licenseService.licenseAllocateRequest(licensePRoductAllocateRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 지급 취소", tags = "BillingSystem")
    @PostMapping("/deallocate")
    public ResponseEntity<EncodingRequestResponse> licenseProductDeallocateToUser(@RequestBody @Valid LicenseProductDeallocateRequest licenseDeallocateRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        EncodingRequestResponse responseMessage = this.licenseService.licenseDeallocateRequest(licenseDeallocateRequest);
        return ResponseEntity.ok(responseMessage);
    }

    @ApiOperation(value = "상품 지급 가능 여부 조회", tags = "BillingSystem")
    @PostMapping("/allocate/check")
    public ResponseEntity<EncodingRequestResponse> licenseAllocateCheckRequest(@RequestBody @Valid LicenseAllocateCheckRequest allocateCheckRequest, BindingResult result) {
        if (result.hasErrors()) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        EncodingRequestResponse responseMessage = this.licenseService.licenseAllocateCheckRequest(allocateCheckRequest);
        return ResponseEntity.ok(responseMessage);
    }
}

