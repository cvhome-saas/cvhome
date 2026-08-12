package com.asrevo.cvhome.podregistry.commons.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the pod registry — the pods themselves, their lifecycle, and where a new store may be placed.
 */
public enum PodRegistryErrors implements ErrorCode {

    /** No pod is registered under the requested id. */
    POD_NOT_FOUND("POD_REGISTRY.POD.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** Pod names are the routing handle and must stay unique. */
    POD_NAME_TAKEN("POD_REGISTRY.POD.NAME_TAKEN", ErrorCategory.CONFLICT),

    /**
     * Nothing could take the store: no pod is active, healthy and under its ceiling. Never falls back to a pod the
     * caller's organization does not own — see {@code NoEligiblePodException}.
     */
    NO_ELIGIBLE_POD("POD_REGISTRY.PLACEMENT.NO_ELIGIBLE_POD", ErrorCategory.UNPROCESSABLE);

    private final String code;

    private final ErrorCategory category;

    PodRegistryErrors(String code, ErrorCategory category) {
        this.code = code;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

}
