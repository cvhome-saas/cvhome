package com.asrevo.cvhome.checkout.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;

/**
 * The timings of order placement: how long an unpaid order holds its stock per payment type, and how the two jobs
 * pace themselves. All under {@code checkout.*}; every value has a default so a service with no override behaves.
 */
@ConfigurationProperties("checkout")
@Getter
@Setter
public class CheckoutProperties {

    private Placement placement = new Placement();

    private Recovery recovery = new Recovery();

    private Expiry expiry = new Expiry();

    /**
     * The payment window of an order of {@code type}: how long inventory holds the stock and how long the order waits
     * before the expiry job closes it. {@code null} for COD, which is confirmed at placement and never expires.
     */
    public Duration paymentWindow(PaymentType type) {
        return switch (type) {
            case STRIPE -> placement.stripe;
            case PAYPAL -> placement.paypal;
            case MANUAL_TRANSFER -> placement.manualTransfer;
            case COD -> null;
        };
    }

    @Getter
    @Setter
    public static class Placement {

        private Duration stripe = Duration.ofMinutes(30);

        private Duration paypal = Duration.ofMinutes(30);

        private Duration manualTransfer = Duration.ofHours(48);

        /** How much longer an order waits once payment reports PROCESSING or AUTHORIZED. */
        private Duration processingGrace = Duration.ofHours(24);

        /** The expiry handed to payment for a COD transaction, which payment requires to be non-null. */
        private Duration codPaymentExpiry = Duration.ofDays(30);
    }

    @Getter
    @Setter
    public static class Recovery {

        private Duration interval = Duration.ofSeconds(30);

        /** A pending action untouched for this long is picked up by the recovery job. */
        private Duration staleAfter = Duration.ofSeconds(60);

        private int batchSize = 50;

        private int maxAttempts = 10;
    }

    @Getter
    @Setter
    public static class Expiry {

        private Duration interval = Duration.ofSeconds(60);

        private int batchSize = 50;
    }
}
