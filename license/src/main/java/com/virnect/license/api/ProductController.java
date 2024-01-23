package com.virnect.license.api;

import javax.validation.Valid;

import org.springframework.context.annotation.Profile;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.application.ProductService;
import com.virnect.license.dto.product.CreateNewProductRequest;
import com.virnect.license.dto.product.CreateNewProductTypeRequest;
import com.virnect.license.dto.billing.request.ProductInfoUpdateRequest;
import com.virnect.license.dto.billing.request.ProductTypeUpdateRequest;
import com.virnect.license.dto.billing.response.ProductInfoListResponse;
import com.virnect.license.dto.billing.response.ProductInfoResponse;
import com.virnect.license.dto.billing.response.ProductTypeInfoListResponse;
import com.virnect.license.dto.billing.response.ProductTypeInfoResponse;
import com.virnect.license.exception.BillingServiceException;
import com.virnect.license.global.common.ApiResponse;
import com.virnect.license.global.error.ErrorCode;

@Api
@Slf4j
@Profile(value = "!onpremise")
@RestController
@RequiredArgsConstructor
@RequestMapping("/licenses")
public class ProductController {
	private static final String PARAMETER_LOG_MESSAGE = "[PRODUCT_CONTROLLER][PARAMETER ERROR]:: {}";
	private final ProductService productService;

	@ApiOperation(value = "신규 상품 등록")
	@PostMapping("/products")
	public ApiResponse<ProductInfoResponse> createNewProductRequest(
		@RequestBody CreateNewProductRequest createNewProductRequest, BindingResult result
	) {
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
	public ApiResponse<ProductInfoResponse> changeProductInfoRequest(
		@RequestBody @Valid ProductInfoUpdateRequest productInfoUpdateRequest, BindingResult result
	) {
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
	public ApiResponse<ProductTypeInfoResponse> createNewProductTypeInfo(
		@RequestBody @Valid CreateNewProductTypeRequest createNewProductTypeRequest, BindingResult result
	) {
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
	public ApiResponse<ProductTypeInfoResponse> updateProductTypeInfoRequest(
		@RequestBody @Valid ProductTypeUpdateRequest productTypeUpdateRequest, BindingResult result
	) {
		if (result.hasErrors()) {
			result.getAllErrors().forEach(message -> log.error(PARAMETER_LOG_MESSAGE, message));
			throw new BillingServiceException(ErrorCode.ERR_INVALID_REQUEST_PARAMETER);
		}
		return productService.updateProductTypeInfo(productTypeUpdateRequest);
	}
}
