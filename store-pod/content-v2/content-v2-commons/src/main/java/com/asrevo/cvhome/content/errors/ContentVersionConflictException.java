package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

public class ContentVersionConflictException extends DuplicateResourceException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentVersionConflictException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentVersionConflictException expected(long expected, long actual) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_VERSION_CONFLICT, ContentVersionConflictException::new)
                .detail("Content has changed since it was read.")
                .param("expectedVersion", expected)
                .param("currentVersion", actual)
                .build();
    }
}
