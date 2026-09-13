package com.insurance.portal.product.controller;

import com.insurance.portal.common.dto.ApiResponse;
import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.product.dto.CreateProductRequest;
import com.insurance.portal.product.dto.ProductResponse;
import com.insurance.portal.product.dto.UpdateProductRequest;
import com.insurance.portal.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductStatus status) {

        List<ProductResponse> response = productService.getAllProducts(category, status, keyword);

        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Products fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) ProductStatus status) {
        return getProducts(keyword, category, status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProduct(id);

        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product fetched successfully")
                .data(response)
                .build());
    }

    @GetMapping("/compare")
    @PreAuthorize("hasAnyRole('CUSTOMER','AGENT','CLAIMS_OFFICER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> compareProducts(@RequestParam List<Long> ids) {
        List<ProductResponse> response = productService.compareProducts(ids);

        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Products compared successfully")
                .data(response)
                .build());
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('AGENT','CUSTOMER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendations(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer limit) {

        List<ProductResponse> response = productService.getRecommendations(category, keyword, limit);

        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product recommendations fetched successfully")
                .data(response)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ProductResponse>builder()
                .success(true)
                .status(HttpStatus.CREATED.value())
                .message("Product created successfully")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);

        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product deleted successfully")
                .build());
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> activateProduct(@PathVariable Long id) {
        ProductResponse response = productService.activateProduct(id);

        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product activated successfully")
                .data(response)
                .build());
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable Long id) {
        ProductResponse response = productService.deactivateProduct(id);

        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .message("Product deactivated successfully")
                .data(response)
                .build());
    }
}

