package com.banfico.minibanking.beneficiary.repository;

import com.banfico.minibanking.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, Long> {

    List<Beneficiary> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndAccountNumber(
            Long customerId,
            String accountNumber
    );
}