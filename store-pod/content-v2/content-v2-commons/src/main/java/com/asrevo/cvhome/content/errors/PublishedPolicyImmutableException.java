package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

public class PublishedPolicyImmutableException extends OperationNotAllowedException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected PublishedPolicyImmutableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PublishedPolicyImmutableException forId(Long id) {
        return new ErrorBuilder<>(ContentErrors.POLICY_IMMUTABLE, PublishedPolicyImmutableException::new)
                .detail("Published policy versions are immutable; create a new version.")
                .param("id", id)
                .build();
    }
}
