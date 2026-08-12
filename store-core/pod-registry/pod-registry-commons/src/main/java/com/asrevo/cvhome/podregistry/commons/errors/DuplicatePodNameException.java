package com.asrevo.cvhome.podregistry.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A pod already exists with that name.
 *
 * <p>
 * The name is the routing handle — the gateway builds {@code pod-&lt;shortId&gt;} routes and the local hostnames are
 * derived from it — so a duplicate is a genuine conflict rather than a validation nicety. This replaces an
 * {@code IllegalArgumentException("Pod name must be unique")}, which surfaced as a 500 carrying no code, and a
 * losing race that surfaced as a raw {@code DuplicateKeyException}.
 * </p>
 */
public class DuplicatePodNameException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicatePodNameException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicatePodNameException of(String name) {
        return new ErrorBuilder<>(PodRegistryErrors.POD_NAME_TAKEN, DuplicatePodNameException::new)
                .detail("A pod named %s already exists.", name)
                .param("name", name)
                .build();
    }

}
