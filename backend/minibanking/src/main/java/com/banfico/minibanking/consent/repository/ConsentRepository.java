package com.banfico.minibanking.consent.repository;

import com.banfico.minibanking.consent.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsentRepository
        extends JpaRepository<Consent, Long> {

    List<Consent> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );
}