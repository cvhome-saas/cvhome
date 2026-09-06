package com.asrevo.cvhome.checkout.config;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutPropertiesTest {

    @Test
    void thePaymentWindowIsPerTypeAndCodHasNone() {
        CheckoutProperties properties = new CheckoutProperties();
        properties.getPlacement().setManualTransfer(Duration.ofHours(72));

        assertThat(properties.paymentWindow(PaymentType.STRIPE)).isEqualTo(Duration.ofMinutes(30));
        assertThat(properties.paymentWindow(PaymentType.PAYPAL)).isEqualTo(Duration.ofMinutes(30));
        assertThat(properties.paymentWindow(PaymentType.MANUAL_TRANSFER)).isEqualTo(Duration.ofHours(72));
        assertThat(properties.paymentWindow(PaymentType.COD)).isNull();
        assertThat(properties.getRecovery().getMaxAttempts()).isEqualTo(10);
        assertThat(properties.getExpiry().getBatchSize()).isEqualTo(50);
        assertThat(properties.getPlacement().getProcessingGrace()).isEqualTo(Duration.ofHours(24));
    }
}
