package com.insurance.portal.product.service;

import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.product.dto.CreateProductRequest;
import com.insurance.portal.product.dto.ProductResponse;
import com.insurance.portal.product.dto.UpdateProductRequest;

import java.util.List;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    void deleteProduct(Long id);

    ProductResponse activateProduct(Long id);

    ProductResponse deactivateProduct(Long id);

    ProductResponse getProduct(Long id);

    List<ProductResponse> getAllProducts(ProductCategory category, ProductStatus status, String keyword);

    List<ProductResponse> getRecommendations(ProductCategory category, String keyword, Integer limit);

    List<ProductResponse> compareProducts(List<Long> ids);
}

