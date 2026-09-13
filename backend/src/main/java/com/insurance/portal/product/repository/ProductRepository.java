package com.insurance.portal.product.repository;

import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByProductCategoryAndStatus(ProductCategory category, ProductStatus status);

    @Query("""
            select p from Product p
            where (:keyword is null or :keyword = ''
               or lower(p.productName) like lower(concat('%', :keyword, '%'))
               or lower(p.productCode) like lower(concat('%', :keyword, '%'))
               or lower(p.description) like lower(concat('%', :keyword, '%')))
              and (:category is null or p.productCategory = :category)
              and (:status is null or p.status = :status)
            """)
    List<Product> searchProducts(String keyword, ProductCategory category, ProductStatus status);

    List<Product> findByIdIn(List<Long> ids);
}

