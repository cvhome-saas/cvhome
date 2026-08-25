package com.asrevo.cvhome.payment.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A webhook payload did not verify against the store's configured signing secret, so nothing in it can be trusted.
 *
 * <p>
 * On a public webhook endpoint this is an expected condition — a probe, a stale secret after a rotation, or a replayed
 * body — and it is never retryable, which is why it deserves a name a caller can catch on rather than a shared
 * validation type.
 * </p>
 */
public class InvalidWebhookSignatureException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidWebhookSignatureException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param provider         payment provider whose webhook failed, e.g. {@code stripe}
     * @param signaturePresent whether a signature header was sent at all — distinguishes an unsigned request from a
     *                         wrongly signed one, which is usually a secret mismatch
     */
    public static InvalidWebhookSignatureException verificationFailed(String provider, boolean signaturePresent,
            Throwable cause) {
        return new ErrorBuilder<>(PaymentErrors.WEBHOOK_SIGNATURE_INVALID, InvalidWebhookSignatureException::new)
                .detail("Webhook signature verification failed for provider %s.", provider)
                .param("provider", provider)
                .param("signaturePresent", signaturePresent)
                .cause(cause)
                .build();
    }

}
