package com.asrevo.cvhome.payment.reads;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.spring.StoreScopedKeyGenerator;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;

/** The payment types a store accepts, a minute per store; a configuration's commit drops the store's entry. */
@Component
@RequiredArgsConstructor
public class PaymentTypeReads {

    private final PaymentConfigurationService configurations;

    @Cacheable(cacheNames = PaymentRegions.Names.TYPES, keyGenerator = StoreScopedKeyGenerator.BEAN)
    public PaymentType[] supported(StoreMerchantId store) {
        return configurations.getSupportedPaymentTypes(store);
    }
}
