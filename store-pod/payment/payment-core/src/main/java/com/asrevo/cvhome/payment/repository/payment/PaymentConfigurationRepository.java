package com.asrevo.cvhome.payment.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

public interface PaymentConfigurationRepository extends JpaRepository<PaymentConfiguration, Long> {

    Optional<PaymentConfiguration> findByStoreMerchantIdAndPaymentType(StoreMerchantId storeMerchantId, PaymentType paymentType);

    List<PaymentConfiguration> findAllByStoreMerchantId(StoreMerchantId storeMerchantId);

}
