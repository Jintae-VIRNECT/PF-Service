package com.virnect.license.api;

import com.virnect.license.application.ProductService;
import com.virnect.license.dto.request.CreateNewProductRequest;
import com.virnect.license.dto.request.CreateNewProductTypeRequest;
import com.virnect.license.dto.request.billing.ProductInfoUpdateRequest;
import com.virnect.license.dto.request.billing.ProductTypeUpdateRequest;
import com.virnect.license.dto.response.biling.ProductInfoListResponse;
import com.virnect.license.dto.response.biling.ProductInfoResponse;
import com.virnect.license.dto.response.biling.ProductTypeInfoListResponse;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.error.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class ProductController {
    private final ProductService productService;
    private static String PARAMETER_LOG_MESSAGE = "[PRODUCT_CONTROLLER][PARAMETER ERROR]:: {}";

    @ApiOperation(value = "신규 상품 등록")
    @PostMapping("/products")
    public ApiResponse<ProductInfoListResponse> createNewProductRequest(@RequestBody CreateNewProductRequest createNewProductRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
            throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return productService.createNewProductHandler(createNewProductRequest);
    }

    @ApiOperation(value = "상품 정보 조회")
    @GetMapping("/products")
    public ApiResponse<ProductInfoListResponse> getAllProductInfoRequest() {
        return productService.getAllProductInfo();
    }

    @ApiOperation(value = "상품 정보 수정")
    @PutMapping("/products")
    public ApiResponse<ProductInfoResponse> changeProductInfoRequest(@RequestBody @Valid ProductInfoUpdateRequest productInfoUpdateRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
            throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return productService.updateProductInfo(productInfoUpdateRequest);
    }

    @ApiOperation(value = "상품 삭제", notes = "상품 데이터는 실제로 삭제되진 않으며, 목록에서 표출만 되지않는다")
    @DeleteMapping("/product/{productId}")
    public ApiResponse<ProductInfoListResponse> deleteProductRequest(@PathVariable("productId") long productId) {
        if (productId <= 0) {
            throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return productService.deleteProduct(productId);
    }

    @ApiOperation(value = "상품 타입 등록")
    @PostMapping("/products/types")
    public ApiResponse<ProductTypeInfoListResponse> createNewProductTypeInfo(@RequestBody @Valid CreateNewProductTypeRequest createNewProductTypeRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
            throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return productService.createNewProductTypeHandler(createNewProductTypeRequest);
    }

    @ApiOperation(value = "상품 타입 정보 조회")
    @GetMapping("/products/types")
    public ApiResponse<ProductTypeInfoListResponse> getAllProductTypeInRequest() {
        return productService.getAllProductTypeInfo();
    }

    @ApiOperation(value = "상품 타입 정보 수정")
    @PutMapping("/products/types")
    public ApiResponse<ProductTypeInfoListResponse> updateProductTypeInfoRequest(@RequestBody @Valid ProductTypeUpdateRequest productTypeUpdateRequest, BindingResult result) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
            throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
        }
        return productService.updateProductTypeInfo(productTypeUpdateRequest);
    }
}
