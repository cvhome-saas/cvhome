package com.asrevo.cvhome.payment.repository.payment;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findTopByRefAndStoreMerchantIdOrderByTransactionDateDesc(String ref, StoreMerchantId storeMerchantId);
}
