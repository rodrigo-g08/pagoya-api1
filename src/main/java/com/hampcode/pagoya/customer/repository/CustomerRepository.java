package com.hampcode.pagoya.customer.repository;

import com.hampcode.pagoya.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByDni(String dni);
    boolean existsByUser_Id(Long userId);
    Optional<Customer> findByUser_Id(Long userId);
    Optional<Customer> findByDni(String dni);
}
