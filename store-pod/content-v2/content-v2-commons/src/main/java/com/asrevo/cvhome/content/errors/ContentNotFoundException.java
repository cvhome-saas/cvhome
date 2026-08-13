package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

public class ContentNotFoundException extends ResourceNotFoundException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected ContentNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ContentNotFoundException forId(Long id) {
        return new ErrorBuilder<>(ContentErrors.CONTENT_NOT_FOUND, ContentNotFoundException::new)
                .detail("Content was not found.")
                .param("contentId", id)
                .build();
    }
}
