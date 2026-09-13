package com.insurance.portal.customer.repository;

import com.insurance.portal.customer.entity.Customer;
import com.insurance.portal.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByUsername(String username);

    Optional<Customer> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Customer> findByUsernameOrEmail(String username, String email);

    Page<Customer> findByRole(Role role, Pageable pageable);
}
