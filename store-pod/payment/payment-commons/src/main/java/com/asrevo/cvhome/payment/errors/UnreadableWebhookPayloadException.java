package com.asrevo.cvhome.payment.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A webhook was authentic but its event body could not be deserialized — typically the provider sent a newer API
 * version than the SDK in use understands.
 *
 * <p>
 * Distinct from {@link InvalidWebhookSignatureException} because the remedy is different: this one points at an SDK or
 * API-version upgrade, not at a secret.
 * </p>
 */
public class UnreadableWebhookPayloadException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnreadableWebhookPayloadException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UnreadableWebhookPayloadException of(String provider, String eventId, String eventType,
            Throwable cause) {
        return new ErrorBuilder<>(PaymentErrors.WEBHOOK_PAYLOAD_UNREADABLE, UnreadableWebhookPayloadException::new)
                .detail("Could not deserialize the %s event data object.", provider)
                .param("provider", provider)
                .param("eventId", eventId)
                .param("eventType", eventType)
                .cause(cause)
                .build();
    }

}
