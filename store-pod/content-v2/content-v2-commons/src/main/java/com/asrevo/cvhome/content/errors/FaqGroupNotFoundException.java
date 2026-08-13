package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

public class FaqGroupNotFoundException extends ResourceNotFoundException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected FaqGroupNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static FaqGroupNotFoundException forId(Long id) {
        return new ErrorBuilder<>(ContentErrors.FAQ_GROUP_NOT_FOUND, FaqGroupNotFoundException::new)
                .detail("FAQ group was not found.")
                .param("id", id)
                .build();
    }
}
