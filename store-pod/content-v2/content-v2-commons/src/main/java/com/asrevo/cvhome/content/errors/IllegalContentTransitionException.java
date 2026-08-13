package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

public class IllegalContentTransitionException extends OperationNotAllowedException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected IllegalContentTransitionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IllegalContentTransitionException from(Object from, Object to) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_TRANSITION_ILLEGAL, IllegalContentTransitionException::new)
                .detail("Content cannot move from %s to %s.", from, to)
                .param("fromStatus", from)
                .param("toStatus", to)
                .build();
    }
}
