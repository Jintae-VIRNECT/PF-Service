package com.virnect.license.application;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.virnect.license.dao.product.ProductRepository;
import com.virnect.license.dao.product.ProductTypeRepository;
import com.virnect.license.domain.product.Product;
import com.virnect.license.domain.product.ProductStatus;
import com.virnect.license.domain.product.ProductType;
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

@Slf4j
@Profile(value = "!onpremise")
@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductRepository productRepository;
	private final ProductTypeRepository productTypeRepository;
	private final ModelMapper modelMapper;

	/**
	 * 상품 생성
	 *
	 * @param createNewProductRequest
	 * @return
	 */
	@Transactional
	public ApiResponse<ProductInfoResponse> createNewProductHandler(
		CreateNewProductRequest createNewProductRequest
	) {
		ProductType productType = productTypeRepository.findById(createNewProductRequest.getProductTypeId())
			.orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_CREATE));
		Product newProduct = Product.builder()
			.name(createNewProductRequest.getProductName())
			.maxStorageSize(createNewProductRequest.getMaxStorageSize())
			.maxDownloadHit(createNewProductRequest.getMaxDownloadHit())
			.maxCallTime(createNewProductRequest.getMaxCallTime())
			.billProductId(createNewProductRequest.getBillingProductId())
			.productType(productType)
			.build();
		productRepository.save(newProduct);

		ProductInfoResponse productInfoResponse = modelMapper.map(newProduct, ProductInfoResponse.class);
		return new ApiResponse<>(productInfoResponse);
	}

	/**
	 * 상품 삭제
	 *
	 * @param productId
	 * @return
	 */
	@Transactional
	public ApiResponse<ProductInfoListResponse> deleteProduct(long productId) {
		long result = productRepository.updateProductDisplayStatusToInactive(productId);
		if (result <= 0) {
			throw new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_DISABLE);
		}
		return getAllProductInfo();
	}

	/**
	 * 상품 타입정보 수정
	 *
	 * @param productTypeUpdateRequest
	 * @return
	 */
	@Transactional
	public ApiResponse<ProductTypeInfoResponse> updateProductTypeInfo(
		ProductTypeUpdateRequest productTypeUpdateRequest
	) {
		ProductType productType = productTypeRepository.findById(productTypeUpdateRequest.getProductTypeId())
			.orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_TYPE_INFO_UPDATE));
		productType.setName(productTypeUpdateRequest.getProductTypeName());
		productTypeRepository.save(productType);
		ProductTypeInfoResponse productTypeInfoResponse = modelMapper.map(productType, ProductTypeInfoResponse.class);
		return new ApiResponse<>(productTypeInfoResponse);
	}

	/**
	 * 상품 타입 생성
	 *
	 * @param createNewProductTypeRequest
	 * @return
	 */
	@Transactional
	public ApiResponse<ProductTypeInfoResponse> createNewProductTypeHandler(
		CreateNewProductTypeRequest createNewProductTypeRequest
	) {
		ProductType productType = new ProductType(createNewProductTypeRequest.getProductTypeName());
		productTypeRepository.save(productType);
		ProductTypeInfoResponse productTypeInfoResponse = modelMapper.map(
			productType,
			ProductTypeInfoResponse.class
		);
		return new ApiResponse<>(productTypeInfoResponse);
	}

	/**
	 * 전체 상품 정보 조회
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	public ApiResponse<ProductInfoListResponse> getAllProductInfo() {
		List<Product> productList = productRepository.findAllByDisplayStatus(ProductStatus.ACTIVE);
		List<ProductInfoResponse> productInfoList = new ArrayList<>();
		productList.forEach(product -> {
			ProductInfoResponse productInfo = modelMapper.map(product, ProductInfoResponse.class);
			productInfoList.add(productInfo);
		});
		return new ApiResponse<>(new ProductInfoListResponse(productInfoList));
	}

	/**
	 * 전체 상품 타입 정보 조회
	 *
	 * @return
	 */
	@Transactional(readOnly = true)
	public ApiResponse<ProductTypeInfoListResponse> getAllProductTypeInfo() {
		List<ProductType> productTypeList = productTypeRepository.findAll();
		List<ProductTypeInfoResponse> productTypeInfoList = new ArrayList<>();
		productTypeList.forEach(productType -> {
			ProductTypeInfoResponse productTypeInfoResponse = modelMapper.map(
				productType,
				ProductTypeInfoResponse.class
			);
			productTypeInfoList.add(productTypeInfoResponse);
		});
		return new ApiResponse<>(new ProductTypeInfoListResponse(productTypeInfoList));
	}

	/**
	 * 상품 정보 수정
	 *
	 * @param updateRequest
	 * @return
	 */
	@Transactional
	public ApiResponse<ProductInfoResponse> updateProductInfo(ProductInfoUpdateRequest updateRequest) {
		Product product = productRepository.findById(updateRequest.getProductId())
			.orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_NOT_FOUND));

		// max call time update
		if (updateRequest.getProductMaxCallTime() > 0) {
			product.setMaxCallTime(updateRequest.getProductMaxCallTime());
		}
		// max download hits update
		if (updateRequest.getProductMaxDownloadHit() > 0) {
			product.setMaxDownloadHit(updateRequest.getProductMaxDownloadHit());
		}
		// max storage update
		if (updateRequest.getProductMaxStorageSize() > 0) {
			product.setMaxStorageSize(updateRequest.getProductMaxStorageSize());
		}
		// productType update
		if (updateRequest.getProductTypeId() > 0) {
			ProductType productType = productTypeRepository.findById(updateRequest.getProductTypeId())
				.orElseThrow(() -> new BillingServiceException(ErrorCode.ERR_BILLING_PRODUCT_INFO_UPDATE));
			product.setProductType(productType);
		}

		// product display option update
		if (updateRequest.getProductDisplayStatus() != null) {
			product.setDisplayStatus(ProductStatus.valueOf(updateRequest.getProductDisplayStatus()));
		}

		productRepository.save(product);

		ProductInfoResponse productInfoResponse = modelMapper.map(product, ProductInfoResponse.class);
		return new ApiResponse<>(productInfoResponse);
	}
}
