package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

public class InvalidFaqReorderException extends OperationNotAllowedException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidFaqReorderException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvalidFaqReorderException because(String reason) {
        return new ErrorBuilder<>(ContentErrors.FAQ_REORDER_INVALID, InvalidFaqReorderException::new)
                .detail("FAQ reorder request is invalid.")
                .param("reason", reason)
                .build();
    }
}
