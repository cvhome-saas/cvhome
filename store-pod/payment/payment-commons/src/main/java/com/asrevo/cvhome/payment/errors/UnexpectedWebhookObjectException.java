package com.asrevo.cvhome.payment.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A webhook event carried a data object of a different type than its event type implies — a
 * {@code checkout.session.completed} whose payload is not a session, for instance.
 *
 * <p>
 * Almost always a mapping bug on this side rather than bad input from the provider, so it is worth its own name: it
 * says "the code that routes event types is wrong", which no shared validation type can.
 * </p>
 */
public class UnexpectedWebhookObjectException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnexpectedWebhookObjectException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UnexpectedWebhookObjectException of(String provider, String eventId, String eventType,
            Class<?> expectedType, Throwable cause) {
        return new ErrorBuilder<>(PaymentErrors.WEBHOOK_UNEXPECTED_OBJECT, UnexpectedWebhookObjectException::new)
                .detail("%s event data object is not of the expected type %s.", provider, expectedType.getSimpleName())
                .param("provider", provider)
                .param("eventId", eventId)
                .param("eventType", eventType)
                .param("expectedType", expectedType.getSimpleName())
                .cause(cause)
                .build();
    }

}
