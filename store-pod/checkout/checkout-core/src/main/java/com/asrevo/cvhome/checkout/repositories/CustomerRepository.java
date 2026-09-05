package com.asrevo.cvhome.checkout.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.asrevo.cvhome.checkout.entity.Customer;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    Optional<Customer> findByStoreMerchantIdAndCuaExternalId(StoreMerchantId store, String cuaExternalId);

    Optional<Customer> findByStoreMerchantIdAndId(StoreMerchantId store, Long id);
}
