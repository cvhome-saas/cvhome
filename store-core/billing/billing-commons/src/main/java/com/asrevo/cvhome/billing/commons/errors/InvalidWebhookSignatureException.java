package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A webhook payload did not verify against the configured signing secret.
 *
 * <p>
 * An expected condition on a public endpoint, not an incident: anyone can POST to it. It is answered with 400 rather
 * than 500 precisely so the provider stops retrying a payload that will never verify.
 * </p>
 */
public class InvalidWebhookSignatureException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidWebhookSignatureException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param provider         the provider that signed the payload
     * @param signaturePresent whether a signature header was sent at all — a missing one usually means a misdirected
     *                         request, a present-but-wrong one usually means a stale signing secret
     * @param cause            the verification failure
     */
    public static InvalidWebhookSignatureException verificationFailed(String provider, boolean signaturePresent,
                                                                     Throwable cause) {
        return new ErrorBuilder<>(BillingErrors.WEBHOOK_SIGNATURE_INVALID, InvalidWebhookSignatureException::new)
                .detail("Could not verify the %s webhook signature.", provider)
                .param("provider", provider)
                .param("signaturePresent", signaturePresent)
                .cause(cause)
                .build();
    }

}
