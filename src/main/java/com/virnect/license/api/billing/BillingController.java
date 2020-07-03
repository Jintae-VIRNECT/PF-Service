package com.virnect.license.api.billing;

import com.virnect.license.application.LicenseService;
import com.virnect.license.dto.request.*;
import com.virnect.license.dto.response.LicenseProductAllocateCheckResponse;
import com.virnect.license.dto.response.LicenseProductAllocateResponse;
import com.virnect.license.dto.response.LicenseProductDeallocateResponse;
import com.virnect.license.dto.response.biling.ProductInfoListResponse;
import com.virnect.license.dto.response.biling.ProductInfoResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoListResponse;
import com.virnect.license.exception.LicenseServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.error.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class BillingController {
    private final LicenseService licenseService;
    private static String PARAMETER_LOG_MESSAGE = "[PARAMETER ERROR]:: {}";

    @ApiOperation(value = "신규 상품 등록")
    @PostMapping("/products")
    public ApiResponse<ProductInfoListResponse> createNewProductRequest(@RequestBody CreateNewProductRequest createNewProductRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.createNewProductHandler(createNewProductRequest);
    }

    @ApiOperation(value = "상품 정보 조회")
    @GetMapping("/products")
    public ApiResponse<ProductInfoListResponse> getAllProductInfoRequest() {
        return this.licenseService.getAllProductInfo();
    }

    @ApiOperation(value = "상품 정보 수정")
    @PutMapping("/products")
    public ApiResponse<ProductInfoResponse> changeProductInfoRequest(@RequestBody @Valid ProductInfoUpdateRequest productInfoUpdateRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.updateProductInfo(productInfoUpdateRequest);
    }

    @ApiOperation(value = "상품 삭제", notes = "상품 데이터는 실제로 삭제되진 않으며, 목록에서 표출만 되지않는다")
    @DeleteMapping("/product/{productId}")
    public ApiResponse<ProductInfoListResponse> deleteProductRequest(@PathVariable("productId") long productId) {
        if (productId <= 0) {
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.deleteProduct(productId);
    }

    @ApiOperation(value = "상품 타입 등록")
    @PostMapping("/products/types")
    public ApiResponse<ProductTypeInfoListResponse> createNewProductTypeInfo(@RequestBody @Valid CreateNewProductTypeRequest createNewProductTypeRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.createNewProductTypeHandler(createNewProductTypeRequest);
    }

    @ApiOperation(value = "상품 타입 정보 조회")
    @GetMapping("/products/types")
    public ApiResponse<ProductTypeInfoListResponse> getAllProductTypeInRequest() {
        return this.licenseService.getAllProductTypeInfo();
    }

    @ApiOperation(value = "상품 타입 정보 수정")
    @PutMapping("/products/types")
    public ApiResponse<ProductTypeInfoListResponse> updateProductTypeInfoRequest(@RequestBody @Valid ProductTypeUpdateRequest productTypeUpdateRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.updateProductTypeInfo(productTypeUpdateRequest);
    }

    @ApiOperation(value = "상품 지급")
    @PostMapping("/allocate")
    public ApiResponse<LicenseProductAllocateResponse> licenseProductAllocateToUser(@RequestBody @Valid LicenseProductAllocateRequest licensePRoductAllocateRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.licenseAllocateRequest(licensePRoductAllocateRequest);
    }

    @ApiOperation(value = "상품 지급 취소")
    @PostMapping("/deallocate")
    public ApiResponse<LicenseProductDeallocateResponse> licenseProductDeallocateToUser(@RequestBody @Valid LicenseProductDeallocateRequest licenseDeallocateRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.licenseDeallocateRequest(licenseDeallocateRequest);
    }

    @ApiOperation(value = "상품 지급 가능 여부 조회")
    @PostMapping("/allocate/check")
    public ApiResponse<LicenseProductAllocateCheckResponse> licenseAllocateCheckRequest(@RequestBody @Valid LicenseAllocateCheckRequest allocateCheckRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(System.out::println);
            throw new LicenseServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return this.licenseService.licenseAllocateCheckRequest(allocateCheckRequest);
    }
}
