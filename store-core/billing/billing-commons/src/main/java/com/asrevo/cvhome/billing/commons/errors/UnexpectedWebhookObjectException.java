package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A webhook carried a data object of a different type than its event type implies.
 *
 * <p>
 * Kept apart from {@link UnreadableWebhookPayloadException} because the remedy differs: that one is a body we could
 * not parse, this one is a body we parsed into something we did not expect, which usually means our handler is
 * matched to the wrong event type.
 * </p>
 */
public class UnexpectedWebhookObjectException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnexpectedWebhookObjectException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param provider  the provider that sent the event
     * @param eventId   the provider's event id
     * @param eventType the event type
     * @param expected  the type the handler expected
     * @param cause     the cast failure
     */
    public static UnexpectedWebhookObjectException of(String provider, Object eventId, Object eventType,
                                                      Class<?> expected, Throwable cause) {
        return new ErrorBuilder<>(BillingErrors.WEBHOOK_UNEXPECTED_OBJECT, UnexpectedWebhookObjectException::new)
                .detail("Event %s of type %s did not carry a %s.", eventId, eventType, expected.getSimpleName())
                .param("provider", provider)
                .param("eventId", eventId)
                .param("eventType", eventType)
                .param("expected", expected.getSimpleName())
                .build();
    }

}
