package com.insurance.portal.product.service;

import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.exception.BadRequestException;
import com.insurance.portal.exception.ResourceNotFoundException;
import com.insurance.portal.product.dto.CreateProductRequest;
import com.insurance.portal.product.dto.ProductResponse;
import com.insurance.portal.product.dto.UpdateProductRequest;
import com.insurance.portal.product.entity.Product;
import com.insurance.portal.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new BadRequestException("Product code already exists.");
        }

        Product product = Product.builder()
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .productCategory(request.getProductCategory())
                .coverageAmount(request.getCoverageAmount())
                .premiumAmount(request.getPremiumAmount())
                .description(request.getDescription())
                .policyTenureMonths(request.getPolicyTenureMonths())
                .status(ProductStatus.ACTIVE)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = getProductEntity(id);

        if (!product.getProductCode().equalsIgnoreCase(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new BadRequestException("Product code already exists.");
        }

        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setProductCategory(request.getProductCategory());
        product.setCoverageAmount(request.getCoverageAmount());
        product.setPremiumAmount(request.getPremiumAmount());
        product.setDescription(request.getDescription());
        product.setPolicyTenureMonths(request.getPolicyTenureMonths());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = getProductEntity(id);
        productRepository.delete(product);
    }

    @Override
    public ProductResponse activateProduct(Long id) {
        Product product = getProductEntity(id);
        product.setStatus(ProductStatus.ACTIVE);
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse deactivateProduct(Long id) {
        Product product = getProductEntity(id);
        product.setStatus(ProductStatus.INACTIVE);
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProduct(Long id) {
        return toResponse(getProductEntity(id));
    }

    @Override
    public List<ProductResponse> getAllProducts(ProductCategory category, ProductStatus status, String keyword) {
        ProductStatus effectiveStatus = status == null ? ProductStatus.ACTIVE : status;

        return productRepository.searchProducts(keyword, category, effectiveStatus)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> getRecommendations(ProductCategory category, String keyword, Integer limit) {
        int maxResults = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);

        return productRepository.searchProducts(keyword, category, ProductStatus.ACTIVE)
                .stream()
                .sorted((p1, p2) -> p1.getPremiumAmount().compareTo(p2.getPremiumAmount()))
                .limit(maxResults)
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ProductResponse> compareProducts(List<Long> ids) {
        if (ids == null || ids.size() < 2) {
            throw new BadRequestException("At least two product ids are required for comparison.");
        }

        if (ids.size() > 4) {
            throw new BadRequestException("You can compare up to 4 products at a time.");
        }

        return productRepository.findByIdIn(ids).stream().map(this::toResponse).toList();
    }

    private Product getProductEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .productCategory(product.getProductCategory())
                .description(product.getDescription())
                .coverageAmount(product.getCoverageAmount())
                .premiumAmount(product.getPremiumAmount())
                .policyTenureMonths(product.getPolicyTenureMonths())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}

