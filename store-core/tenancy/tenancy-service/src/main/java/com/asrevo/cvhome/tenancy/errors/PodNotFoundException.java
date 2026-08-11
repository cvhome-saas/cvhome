package com.asrevo.cvhome.tenancy.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No pod is registered under the requested id.
 *
 * <p>
 * It was a legacy {@code ResourceNotFoundException} carrying the sentence "Pod not found" and no code, so an operator
 * updating a pod that had been removed could not tell it apart from any other 404 the control plane returns.
 * </p>
 */
public class PodNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PodNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PodNotFoundException of(Object podId) {
        return new ErrorBuilder<>(TenancyErrors.POD_NOT_FOUND, PodNotFoundException::new)
                .detail("No pod is registered with id %s.", podId)
                .param("podId", podId)
                .build();
    }

}
