package com.asrevo.cvhome.content.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

public class PolicyNotFoundException extends ResourceNotFoundException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected PolicyNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PolicyNotFoundException forId(Long id) {
        return new ErrorBuilder<>(ContentErrors.POLICY_NOT_FOUND, PolicyNotFoundException::new)
                .detail("Policy version was not found.")
                .param("id", id)
                .build();
    }
}
