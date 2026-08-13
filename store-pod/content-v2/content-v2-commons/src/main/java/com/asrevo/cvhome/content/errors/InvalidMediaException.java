package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

public class InvalidMediaException extends ValidationException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected InvalidMediaException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InvalidMediaException because(String reason) {
        return new ErrorBuilder<>(ContentErrors.MEDIA_INVALID, InvalidMediaException::new)
                .detail("Media upload is invalid.")
                .param("reason", reason)
                .build();
    }
}
