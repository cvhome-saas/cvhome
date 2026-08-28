package com.asrevo.cvhome.errors;

/**
 * Broad classification of a failure. The category is what determines the HTTP status of the response, so an
 * {@link ErrorCode} only has to declare <em>what kind</em> of failure it is — never a raw status number.
 *
 * <p>
 * The status is held as a plain {@code int} so this module stays free of any Spring dependency.
 * </p>
 */
public enum ErrorCategory {

    /**
     * Request payload failed validation; carries field-level errors where available.
     */
    VALIDATION(400),

    /**
     * Request was syntactically or semantically malformed (unparseable body, bad parameter type).
     */
    MALFORMED(400),

    /**
     * A value could not be converted or parsed (price, country code, date).
     */
    CONVERSION(400),

    /**
     * No credentials, or credentials that could not be authenticated.
     */
    UNAUTHENTICATED(401),

    /**
     * Authenticated, but not permitted to perform the operation or reach the resource.
     */
    FORBIDDEN(403),

    /**
     * The addressed resource does not exist.
     */
    NOT_FOUND(404),

    /**
     * The operation conflicts with current state (duplicate code, resource in use).
     */
    CONFLICT(409),

    /**
     * Upload or request body exceeded the configured limit.
     */
    PAYLOAD_TOO_LARGE(413),

    /**
     * Well-formed request that a business rule refuses to carry out.
     */
    UNPROCESSABLE(422),

    /**
     * Persistence, object storage or other infrastructure failure.
     */
    STORAGE(500),

    /**
     * Unclassified server-side failure.
     */
    INTERNAL(500),

    /**
     * A downstream service returned an error that this service cannot resolve.
     */
    REMOTE_SERVICE(502),

    /**
     * A downstream service did not respond in time.
     */
    TIMEOUT(504);

    private final int httpStatus;

    ErrorCategory(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }

    public boolean isClientError() {
        return httpStatus >= 400 && httpStatus < 500;
    }

}
