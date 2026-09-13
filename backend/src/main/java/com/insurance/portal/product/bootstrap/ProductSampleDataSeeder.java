package com.insurance.portal.product.bootstrap;

import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.product.entity.Product;
import com.insurance.portal.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.product.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ProductSampleDataSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<Product> sampleProducts = List.of(
                buildProduct(
                        "PRD-HEALTH-001",
                        "Health Insurance Basic",
                        ProductCategory.HEALTH,
                        "Covers hospitalization and essential medical expenses for individuals.",
                        new BigDecimal("300000.00"),
                        new BigDecimal("8500.00"),
                        12,
                        ProductStatus.ACTIVE
                ),
                buildProduct(
                        "PRD-MOTOR-001",
                        "Motor Insurance Standard",
                        ProductCategory.MOTOR,
                        "Covers accidental damage and third-party liabilities for private vehicles.",
                        new BigDecimal("500000.00"),
                        new BigDecimal("12000.00"),
                        12,
                        ProductStatus.ACTIVE
                ),
                buildProduct(
                        "PRD-LIFE-001",
                        "Life Insurance Secure",
                        ProductCategory.LIFE,
                        "Long-term life protection with family benefit and maturity payout.",
                        new BigDecimal("1500000.00"),
                        new BigDecimal("18000.00"),
                        24,
                        ProductStatus.ACTIVE
                ),
                buildProduct(
                        "PRD-TRAVEL-001",
                        "Travel Insurance Plus",
                        ProductCategory.TRAVEL,
                        "Trip protection including emergency medical, baggage loss, and delays.",
                        new BigDecimal("200000.00"),
                        new BigDecimal("3500.00"),
                        6,
                        ProductStatus.ACTIVE
                )
        );

        try {
            int inserted = 0;
            for (Product product : sampleProducts) {
                if (!productRepository.existsByProductCode(product.getProductCode())) {
                    productRepository.save(product);
                    inserted++;
                }
            }

            log.info("Product seed completed. Inserted: {}, Skipped: {}", inserted, sampleProducts.size() - inserted);
        } catch (DataAccessException ex) {
            log.warn("Skipping product seed because product schema is not ready yet. Apply DB migration and restart.", ex);
        }
    }

    private Product buildProduct(String productCode,
                                 String productName,
                                 ProductCategory category,
                                 String description,
                                 BigDecimal coverageAmount,
                                 BigDecimal premiumAmount,
                                 int policyTenureMonths,
                                 ProductStatus status) {
        return Product.builder()
                .productCode(productCode)
                .productName(productName)
                .productCategory(category)
                .description(description)
                .coverageAmount(coverageAmount)
                .premiumAmount(premiumAmount)
                .policyTenureMonths(policyTenureMonths)
                .status(status)
                .build();
    }
}
