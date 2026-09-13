package com.insurance.portal.validation;

import com.insurance.portal.claim.entity.Claim;
import com.insurance.portal.claim.repository.ClaimRepository;
import com.insurance.portal.common.enums.ClaimStatus;
import com.insurance.portal.common.enums.Gender;
import com.insurance.portal.common.enums.PaymentMethod;
import com.insurance.portal.common.enums.PaymentStatus;
import com.insurance.portal.common.enums.PolicyStatus;
import com.insurance.portal.common.enums.ProductCategory;
import com.insurance.portal.common.enums.ProductStatus;
import com.insurance.portal.common.enums.RenewalStatus;
import com.insurance.portal.common.enums.CancellationStatus;
import com.insurance.portal.common.enums.Role;
import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.customer.repository.CustomerRepository;
import com.insurance.portal.payment.entity.Payment;
import com.insurance.portal.payment.repository.PaymentRepository;
import com.insurance.portal.policy.entity.Policy;
import com.insurance.portal.policy.repository.PolicyRepository;
import com.insurance.portal.product.entity.Product;
import com.insurance.portal.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.validation-data.enabled", havingValue = "true")
public class ValidationDataBootstrap implements ApplicationRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.validation-data.customer.username}")
    private String customerUsername;
    @Value("${app.validation-data.customer.password}")
    private String customerPassword;
    @Value("${app.validation-data.agent.username}")
    private String agentUsername;
    @Value("${app.validation-data.agent.password}")
    private String agentPassword;
    @Value("${app.validation-data.claims-officer.username}")
    private String claimsOfficerUsername;
    @Value("${app.validation-data.claims-officer.password}")
    private String claimsOfficerPassword;
    @Value("${app.validation-data.admin.username}")
    private String adminUsername;
    @Value("${app.validation-data.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Customer customer = upsertUser(customerUsername, customerPassword, "Validation", "Customer", Role.CUSTOMER, "validation.customer@example.invalid");
        upsertUser(agentUsername, agentPassword, "Validation", "Agent", Role.AGENT, "validation.agent@example.invalid");
        upsertUser(claimsOfficerUsername, claimsOfficerPassword, "Validation", "Claims Officer", Role.CLAIMS_OFFICER, "validation.claims@example.invalid");
        upsertUser(adminUsername, adminPassword, "Validation", "Administrator", Role.ADMIN, "validation.admin@example.invalid");

        Product product = upsertProduct();
        Policy policy = upsertPolicy(customer, product);
        upsertClaim(customer, policy);
        upsertPayment(customer, policy);
    }

    private Customer upsertUser(String username, String rawPassword, String firstName, String lastName, Role role, String email) {
        requireValue(username, role + " username");
        requireValue(rawPassword, role + " password");
        Customer customer = customerRepository.findByUsername(username).orElseGet(Customer::new);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customer.setGender(Gender.OTHER);
        customer.setPhoneNumber("9999999999");
        customer.setEmail(existingEmail(customer, email));
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(rawPassword));
        customer.setRole(role);
        customer.setEnabled(true);
        customer.setAddressLine1("Validation data");
        customer.setCity("Boston");
        customer.setState("MA");
        customer.setCountry("USA");
        customer.setPostalCode("02110");
        return customerRepository.save(customer);
    }

    private String existingEmail(Customer customer, String fallback) {
        return customer.getEmail() == null || customer.getEmail().endsWith("@example.invalid") ? fallback : customer.getEmail();
    }

    private Product upsertProduct() {
        Product product = productRepository.findByProductCode("VAL-HEALTH-001").orElseGet(Product::new);
        product.setProductCode("VAL-HEALTH-001");
        product.setProductName("Validation Health Secure");
        product.setProductCategory(ProductCategory.HEALTH);
        product.setDescription("Controlled validation product for production smoke testing.");
        product.setCoverageAmount(new BigDecimal("300000.00"));
        product.setPremiumAmount(new BigDecimal("9000.00"));
        product.setPolicyTenureMonths(12);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private Policy upsertPolicy(Customer customer, Product product) {
        Policy policy = policyRepository.findByPolicyNumber("POL-VALIDATION-001").orElseGet(Policy::new);
        policy.setPolicyNumber("POL-VALIDATION-001");
        policy.setCustomer(customer);
        policy.setProduct(product);
        policy.setNomineeName("Validation Nominee");
        policy.setNomineeRelation("SPOUSE");
        policy.setStartDate(LocalDate.now().minusDays(10));
        policy.setEndDate(LocalDate.now().plusDays(355));
        policy.setCoverageAmount(product.getCoverageAmount());
        policy.setRenewalPremium(product.getPremiumAmount());
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setRenewalStatus(RenewalStatus.NOT_DUE);
        policy.setCancellationStatus(CancellationStatus.NONE);
        return policyRepository.save(policy);
    }

    private Claim upsertClaim(Customer customer, Policy policy) {
        Claim claim = claimRepository.findByClaimNumber("CLM-VALIDATION-001").orElseGet(Claim::new);
        claim.setClaimNumber("CLM-VALIDATION-001");
        claim.setPolicy(policy);
        claim.setSubmittedBy(customer);
        claim.setIncidentDate(LocalDate.now().minusDays(3));
        claim.setClaimAmount(new BigDecimal("1200.00"));
        claim.setDescription("Controlled validation claim for role workflow smoke testing.");
        claim.setSupportingDocuments("https://example.com/validation/claim-001.pdf");
        claim.setDecisionReason(null);
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setSubmittedDate(LocalDateTime.now().minusDays(2));
        claim.setReviewedDate(null);
        claim.setSettledDate(null);
        return claimRepository.save(claim);
    }

    private Payment upsertPayment(Customer customer, Policy policy) {
        Payment payment = paymentRepository.findByPaymentNumber("PAY-VALIDATION-001").orElseGet(Payment::new);
        payment.setPaymentReference("PAY-VALIDATION-001");
        payment.setPolicy(policy);
        payment.setCustomer(customer);
        payment.setAmount(new BigDecimal("9000.00"));
        payment.setPaymentDate(LocalDateTime.now().minusDays(1));
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESSFUL);
        payment.setTransactionReference("TXN-VALIDATION-001");
        payment.setInvoiceNumber("INV-VALIDATION-001");
        payment.setReceiptNumber("RCT-VALIDATION-001");
        payment.setFailureReason(null);
        return paymentRepository.save(payment);
    }

    private void requireValue(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be provided when validation data is enabled.");
        }
    }
}
