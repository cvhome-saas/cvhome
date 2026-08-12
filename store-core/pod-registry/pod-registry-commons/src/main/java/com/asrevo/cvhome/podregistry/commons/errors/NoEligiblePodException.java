package com.asrevo.cvhome.podregistry.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * Nothing could take the store.
 *
 * <p>
 * Raised in two situations that must not be collapsed. Either no shared pod is currently eligible, or — more
 * pointedly — the organization owns private pods and <em>none of them</em> is eligible. The second case
 * <strong>never</strong> falls back to shared infrastructure: an organization that paid for dedicated pods has not
 * agreed to have its next store quietly placed among strangers, and the caller must be told rather than
 * accommodated.
 * </p>
 *
 * <p>
 * It also replaces an {@code IllegalArgumentException: bound must be positive} — the old selector called
 * {@code random.nextInt(0)} on an empty candidate list, so "no pod available" arrived as a 500 with no code.
 * </p>
 */
public class NoEligiblePodException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected NoEligiblePodException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static NoEligiblePodException of(Object orgId, String reason) {
        return new ErrorBuilder<>(PodRegistryErrors.NO_ELIGIBLE_POD, NoEligiblePodException::new)
                .detail("No pod can take a new store for organization %s: %s", orgId, reason)
                .param("orgId", String.valueOf(orgId))
                .param("reason", reason)
                .build();
    }

}
