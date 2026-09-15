package com.asrevo.cvhome.payment.reads;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotatedElementUtils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfigurationId;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentTypeReadsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    @Mock
    private PaymentConfigurationService configurations;

    @InjectMocks
    private PaymentTypeReads reads;

    @Test
    void theReadDelegatesAndIsDeclaredOnThePaymentRegionAndAConfigurationNamesItsStore() throws Exception {
        PaymentType[] types = {PaymentType.STRIPE};
        when(configurations.getSupportedPaymentTypes(STORE)).thenReturn(types);

        assertThat(reads.supported(STORE)).isSameAs(types);
        Method read = PaymentTypeReads.class.getMethod("supported", StoreMerchantId.class);
        assertThat(AnnotatedElementUtils.findMergedAnnotation(read, Cacheable.class).cacheNames())
                .containsExactly(PaymentRegions.TYPES.regionName());
        assertThat(PaymentRegions.TYPES.regionName()).isEqualTo("payment.types");
        assertThat(PaymentRegions.TYPES.valueType()).isEqualTo(PaymentType[].class);
        assertThat(PaymentRegions.TYPES.ttl().toSeconds()).isEqualTo(60);
        assertThat(PaymentRegions.TYPES.maxSize()).isPositive();

        PaymentConfiguration configuration = new PaymentConfiguration();
        assertThat(configuration.scopedStore()).isNull();
        PaymentConfigurationId id = new PaymentConfigurationId();
        id.setStoreMerchantId(STORE);
        configuration.setId(id);
        assertThat(configuration.scopedStore()).isEqualTo(STORE);
    }
}
