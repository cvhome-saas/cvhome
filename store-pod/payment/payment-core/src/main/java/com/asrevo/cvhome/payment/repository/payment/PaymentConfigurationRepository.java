package com.asrevo.cvhome.payment.repository.payment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfigurationId;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

public interface PaymentConfigurationRepository extends JpaRepository<PaymentConfiguration, PaymentConfigurationId> {

    Optional<PaymentConfiguration> findByIdStoreMerchantIdAndIdPaymentType(StoreMerchantId storeMerchantId, PaymentType paymentType);

    List<PaymentConfiguration> findAllByIdStoreMerchantId(StoreMerchantId storeMerchantId);

}
