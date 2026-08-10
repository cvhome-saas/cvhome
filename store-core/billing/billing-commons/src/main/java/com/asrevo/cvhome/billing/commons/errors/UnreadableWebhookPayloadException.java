package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A webhook verified but its event body could not be deserialized — normally a provider API version we do not
 * understand yet.
 */
public class UnreadableWebhookPayloadException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnreadableWebhookPayloadException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param provider  the provider that sent the event
     * @param eventId   the provider's event id, which is what a support ticket to them will quote
     * @param eventType the event type
     * @param cause     the deserialization failure
     */
    public static UnreadableWebhookPayloadException of(String provider, Object eventId, Object eventType,
                                                       Throwable cause) {
        return new ErrorBuilder<>(BillingErrors.WEBHOOK_PAYLOAD_UNREADABLE, UnreadableWebhookPayloadException::new)
                .detail("Could not read the body of %s event %s.", provider, eventId)
                .param("provider", provider)
                .param("eventId", eventId)
                .param("eventType", eventType)
                .cause(cause)
                .build();
    }

}
