package com.asrevo.cvhome.errors;

/**
 * A stable, machine-readable identifier for one specific failure condition.
 *
 * <p>
 * Implemented by one enum per bounded context ({@code CatalogErrors}, {@code CheckoutErrors}, …) rather than a single
 * central enum, so a context can add codes without touching shared code. Codes are namespaced
 * {@code <CONTEXT>.<RESOURCE>.<CONDITION>}, for example {@code CATALOG.PRODUCT.NOT_FOUND}.
 * </p>
 *
 * <p>
 * The code is part of the public API contract: clients branch on it and resolve translations from it, so renaming a
 * constant's {@link #code()} is a breaking change.
 * </p>
 */
public interface ErrorCode {

    /**
     * The namespaced identifier emitted to clients, e.g. {@code CATALOG.PRODUCT.NOT_FOUND}.
     */
    String code();

    /**
     * The classification that determines the HTTP status of the response.
     */
    ErrorCategory category();

    /**
     * Translation key clients should use to render this error. Defaults to the code itself, which keeps the client
     * vocabulary and the server vocabulary identical unless a context deliberately diverges.
     */
    default String messageKey() {
        return code();
    }

}
