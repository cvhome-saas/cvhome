package com.asrevo.cvhome.errors;

/**
 * Error codes that are not specific to any bounded context — infrastructure, transport and framework-level failures.
 *
 * <p>
 * Anything a domain can name for itself belongs in that context's own enum ({@code CatalogErrors},
 * {@code CheckoutErrors}, …) instead, so this stays small and does not become the dumping ground the single
 * {@code ServiceException} used to be.
 * </p>
 */
public enum CommonErrors implements ErrorCode {

    /**
     * Fallback for an unclassified server-side failure. Detail is never sent to the client.
     */
    INTERNAL_ERROR("COMMON.INTERNAL_ERROR", ErrorCategory.INTERNAL),

    /**
     * Bean validation rejected the request body; accompanied by field errors.
     */
    VALIDATION_FAILED("COMMON.VALIDATION_FAILED", ErrorCategory.VALIDATION),

    /**
     * A parameter or path variable failed its constraints.
     */
    CONSTRAINT_VIOLATION("COMMON.CONSTRAINT_VIOLATION", ErrorCategory.VALIDATION),

    /**
     * Request body could not be parsed, or a parameter had the wrong type.
     */
    MALFORMED_REQUEST("COMMON.MALFORMED_REQUEST", ErrorCategory.MALFORMED),

    /**
     * A required parameter, header or part was absent.
     */
    MISSING_PARAMETER("COMMON.MISSING_PARAMETER", ErrorCategory.VALIDATION),

    /**
     * Generic conversion failure for callers without a context-specific code.
     */
    CONVERSION_FAILED("COMMON.CONVERSION_FAILED", ErrorCategory.CONVERSION),

    /**
     * No credentials were supplied, or they could not be authenticated.
     */
    UNAUTHENTICATED("COMMON.UNAUTHENTICATED", ErrorCategory.UNAUTHENTICATED),

    /**
     * Authenticated, but not permitted to perform this operation.
     */
    ACCESS_DENIED("COMMON.ACCESS_DENIED", ErrorCategory.FORBIDDEN),

    /**
     * Request targeted a store the caller has no access to.
     */
    WRONG_STORE("COMMON.WRONG_STORE", ErrorCategory.FORBIDDEN),

    /**
     * Generic not-found for callers without a context-specific code.
     */
    RESOURCE_NOT_FOUND("COMMON.RESOURCE_NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * HTTP method not supported by the endpoint.
     */
    METHOD_NOT_ALLOWED("COMMON.METHOD_NOT_ALLOWED", ErrorCategory.MALFORMED),

    /**
     * Content type not supported by the endpoint.
     */
    UNSUPPORTED_MEDIA_TYPE("COMMON.UNSUPPORTED_MEDIA_TYPE", ErrorCategory.MALFORMED),

    /**
     * Upload exceeded the configured maximum size.
     */
    UPLOAD_TOO_LARGE("COMMON.UPLOAD_TOO_LARGE", ErrorCategory.PAYLOAD_TOO_LARGE),

    /**
     * Writing an asset to object storage failed.
     */
    ASSET_UPLOAD_FAILED("COMMON.ASSET_UPLOAD_FAILED", ErrorCategory.STORAGE),

    /**
     * Reading an asset from object storage failed.
     */
    ASSET_READ_FAILED("COMMON.ASSET_READ_FAILED", ErrorCategory.STORAGE),

    /**
     * Removing an asset from object storage failed.
     */
    ASSET_DELETE_FAILED("COMMON.ASSET_DELETE_FAILED", ErrorCategory.STORAGE),

    /**
     * The requested asset is not present in object storage.
     */
    ASSET_NOT_FOUND("COMMON.ASSET_NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A downstream service returned an error with no problem body to interpret.
     */
    REMOTE_CALL_FAILED("COMMON.REMOTE_CALL_FAILED", ErrorCategory.REMOTE_SERVICE),

    /**
     * A downstream service could not be reached.
     */
    REMOTE_UNAVAILABLE("COMMON.REMOTE_UNAVAILABLE", ErrorCategory.REMOTE_SERVICE),

    /**
     * A downstream service did not respond in time.
     */
    REMOTE_TIMEOUT("COMMON.REMOTE_TIMEOUT", ErrorCategory.TIMEOUT);

    private final String code;

    private final ErrorCategory category;

    CommonErrors(String code, ErrorCategory category) {
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
