package com.asrevo.cvhome.payment.repository.payment;


import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.payment.entity.payment.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
