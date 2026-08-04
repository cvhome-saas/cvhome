package com.asrevo.cvhome.errors.remote;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.asrevo.cvhome.errors.ErrorCode;

/**
 * The error contract of one service's API, declared by that service's {@code -external-api} module and handed to the
 * client that calls it.
 *
 * <p>
 * An {@code -external-api} module is a client SDK, and decoding failures is part of a client SDK's job: Stripe's turns
 * an error body into {@code CardException} or {@code RateLimitException} so a caller branches on type. This is the same
 * idea — the module states which of its codes have a named exception, and {@code RemoteProblemTranslator} rebuilds one
 * on the caller's side.
 * </p>
 *
 * <p>
 * Published as a constant and passed explicitly when the client is built:
 * </p>
 *
 * <pre>{@code
 * public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
 *         .map(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::from)
 *         .unreachable(PaymentApiUnavailableException::from)
 *         .build();
 *
 * restClientBuilder.buildClient("payment", ExternalPaymentGatewayService.class, PaymentApiErrors.CATALOG);
 * }</pre>
 *
 * <p>
 * This was once an interface discovered through {@link java.util.ServiceLoader}, with an {@code apis()} method naming
 * the {@code @HttpExchange} interfaces it spoke for and a {@code META-INF/services} file per module. All of it existed
 * to answer one question — which contract applies to this client — that the code building the client could always have
 * answered itself, since it names both. Implicit registration earns its keep for an open set of providers; for eleven
 * call sites it only meant a missing or misspelt services file degraded silently, with nothing at compile time to
 * catch it.
 * </p>
 *
 * <p>
 * Naming a code stays optional. Anything unnamed arrives as
 * {@link com.asrevo.cvhome.errors.UnmappedRemoteFailureException} carrying the remote's own code, so a module opts in
 * one code at a time and a client with no contract at all still behaves sensibly.
 * </p>
 */
public final class RemoteErrorCatalog {

    private static final RemoteErrorCatalog NONE = builder().build();

    private final Map<String, RemoteExceptionFactory> mappings;

    private final RemoteExceptionFactory transportFailure;

    private RemoteErrorCatalog(Map<String, RemoteExceptionFactory> mappings,
            RemoteExceptionFactory transportFailure) {
        this.mappings = Collections.unmodifiableMap(new LinkedHashMap<>(mappings));
        this.transportFailure = transportFailure;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * The contract of an API that names none of its failures — what a client built without one gets. Every code then
     * falls back to {@code UnmappedRemoteFailureException}, which is how every caller behaved before catalogs existed.
     */
    public static RemoteErrorCatalog none() {
        return NONE;
    }

    /**
     * The factory for {@code code}, or empty when this API does not name it.
     */
    public Optional<RemoteExceptionFactory> find(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(mappings.get(code));
    }

    /**
     * The exception for "this API could not be reached at all" — connection refused, DNS failure, read timeout. No
     * business decision was reached, so this is deliberately separate from the code mappings, which are answers the
     * remote actually gave. {@code null} falls back to the generic unavailable/timeout pair.
     */
    public RemoteExceptionFactory transportFailure() {
        return transportFailure;
    }

    /**
     * Collects the mappings. Codes are given as {@link ErrorCode} rather than as strings, so renaming one cannot
     * silently orphan its entry here.
     */
    public static final class Builder {

        private final Map<String, RemoteExceptionFactory> mappings = new LinkedHashMap<>();

        private RemoteExceptionFactory transportFailure;

        private Builder() {
        }

        public Builder map(ErrorCode code, RemoteExceptionFactory factory) {
            mappings.put(code.code(), factory);
            return this;
        }

        public Builder unreachable(RemoteExceptionFactory factory) {
            this.transportFailure = factory;
            return this;
        }

        public RemoteErrorCatalog build() {
            return new RemoteErrorCatalog(mappings, transportFailure);
        }

    }

}
