package com.asrevo.cvhome.billing.service.stripe;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.StripeRequestEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.stripe.exception.StripeException;
import com.stripe.net.RequestOptions;

import lombok.RequiredArgsConstructor;

/**
 * What every Stripe gateway needs: request options carrying the credential and an idempotency key, and the record of
 * a mutation having been attempted.
 *
 * <p>
 * The credential is passed per call. Assigning {@code Stripe.apiKey} once, as tenancy's implementation does,
 * makes it process-wide state that no call site can see or override, and silently binds every call in the JVM to
 * whichever key was set last.
 * </p>
 */
@RequiredArgsConstructor
public abstract class StripeGatewaySupport {

    /**
     * The provider these failures are reported against. One constant because a repeated literal is a checkstyle
     * failure, and because the name belongs to the provider rather than to any one call.
     */
    protected static final String STRIPE = "stripe";

    private final StripeCredentials credentials;

    private final StripeRequestRepository stripeRequestRepository;

    /**
     * The status Stripe reported, or {@code 0} when the call never reached it at all.
     *
     * <p>
     * Stripe leaves the status null when there was no response, and {@code 0} is exactly what the provider metadata
     * uses for "no answer" — which is the difference between a refusal and an unknown outcome.
     * </p>
     */
    protected static int statusOf(StripeException e) {
        return e.getStatusCode() == null ? 0 : e.getStatusCode();
    }

    /**
     * A key that makes retrying a call safe. Two attempts at the same operation for the same subject inside the same
     * minute reuse Stripe's stored answer instead of acting twice; a deliberate attempt later gets a fresh one.
     */
    protected static String idempotencyKey(StripeRequestOperation operation, Object subject) {
        return String.format("%s:%s:%s", operation.name().toLowerCase(Locale.ROOT), subject,
                Instant.now().truncatedTo(ChronoUnit.MINUTES).getEpochSecond());
    }

    protected RequestOptions options(String idempotencyKey) {
        return RequestOptions.builder()
                .setApiKey(credentials.apiKey())
                .setIdempotencyKey(idempotencyKey)
                .build();
    }

    /**
     * Options for a read. No idempotency key: reads change nothing, and giving them one would make the request table
     * a log of traffic rather than a record of intent.
     */
    protected RequestOptions readOptions() {
        return RequestOptions.builder().setApiKey(credentials.apiKey()).build();
    }

    /**
     * Records that a mutation is about to be attempted.
     *
     * <p>
     * Runs in its own transaction so the intent survives whatever happens next — including the caller's transaction
     * rolling back. A row with no {@code completed_at} means "this may have reached Stripe", which is precisely the
     * state a retry needs to know about.
     * </p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void recordIntent(String idempotencyKey, StoreMerchantId store, StripeRequestOperation operation) {
        if (!stripeRequestRepository.existsById(idempotencyKey)) {
            stripeRequestRepository.save(StripeRequestEntity.intent(idempotencyKey, store, operation));
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void recordCompletion(String idempotencyKey, String stripeObjectId) {
        stripeRequestRepository.findById(idempotencyKey)
                .ifPresent(it -> stripeRequestRepository.save(it.completed(stripeObjectId)));
    }

}
