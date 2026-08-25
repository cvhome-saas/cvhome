package com.asrevo.cvhome.podregistry.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No pod is registered under the requested id.
 *
 * <p>
 * Replaces the registry's previous behaviour of returning {@code 200} with a {@code null} body — a repository
 * {@code orElse(null)} that reached the controller untouched, so a caller asking for a pod that had been removed
 * could not tell success from absence.
 * </p>
 */
public class PodNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PodNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PodNotFoundException of(PodId podId) {
        String id = podId == null || podId.getId() == null ? "unknown" : podId.getId().toString();
        return new ErrorBuilder<>(PodRegistryErrors.POD_NOT_FOUND, PodNotFoundException::new)
                .detail("No pod is registered with id %s.", id)
                .param("podId", id)
                .build();
    }

}
