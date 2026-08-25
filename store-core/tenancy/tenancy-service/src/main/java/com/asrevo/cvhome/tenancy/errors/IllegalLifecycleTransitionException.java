package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The requested lifecycle move is not one the current status allows.
 *
 * <p>
 * The legal moves live in one table on the status enums rather than as scattered {@code if}s, so a new status
 * cannot quietly become reachable from everywhere.
 * </p>
 */
public class IllegalLifecycleTransitionException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IllegalLifecycleTransitionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IllegalLifecycleTransitionException of(Object id, Object from, Object to) {
        return new ErrorBuilder<>(TenancyErrors.ILLEGAL_LIFECYCLE_TRANSITION,
                IllegalLifecycleTransitionException::new)
                .detail("%s cannot move from %s to %s.", id, from, to)
                .param("id", String.valueOf(id))
                .param("from", String.valueOf(from))
                .param("to", String.valueOf(to))
                .build();
    }

}
