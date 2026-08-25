package com.asrevo.cvhome.errors;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

/**
 * A call to a third-party provider failed — Stripe, PayPal, a shipping carrier, any system outside cvhome reached
 * through its own SDK rather than through an {@code -external-api} client.
 *
 * <p>
 * The sibling of {@link RemoteServiceException}, and the distinction is the whole point of having two types. A
 * {@link RemoteServiceException} crosses a boundary <em>we</em> define: the peer speaks our problem-detail contract, so
 * its {@code code} is one of ours, its status means what our statuses mean, and both can be re-emitted unchanged. A
 * provider shares none of that. {@code card_declined} is Stripe's vocabulary, and Stripe's 402 answers a question the
 * caller never asked.
 * </p>
 *
 * <p>
 * So a provider's code and status are carried for diagnosis and never re-emitted as our own. The response keeps this
 * service's {@link ErrorCode} and the status of its {@link ErrorCategory}; the provider's own values travel as the
 * {@code provider}, {@code providerCode} and {@code providerStatus} extensions, where an operator can read them and no
 * client can mistake them for our contract.
 * </p>
 *
 * <p>
 * Two consequences worth stating, because they were bugs while this shared one type with {@link RemoteServiceException}:
 * a provider code can no longer overwrite the {@code code} a caller's {@code RemoteErrorCatalog} keys on, and a
 * provider's 401 — meaning <em>our</em> API key is wrong — can no longer be reflected back at an end user as though
 * they had failed to authenticate.
 * </p>
 *
 * <p>
 * Abstract like every other category base: subclass it with {@link #of(ErrorCode, Factory)}, which keeps the provider
 * metadata while producing the named type.
 * </p>
 */
public abstract class ExternalProviderException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String provider;

    private final String providerCode;

    private final int providerStatus;

    protected ExternalProviderException(ErrorPayload payload, Throwable cause, String provider, String providerCode,
            int providerStatus) {
        super(payload, cause);
        this.provider = provider;
        this.providerCode = providerCode;
        this.providerStatus = providerStatus;
    }

    /**
     * Builds a condition-named subclass — how a bounded context gets a descriptive exception (say
     * {@code PaymentInitiateRejectedException}) while keeping the provider metadata the web layer reports.
     */
    public static <T extends ExternalProviderException> Builder<T> of(ErrorCode errorCode, Factory<T> factory) {
        return new Builder<>(errorCode, factory);
    }

    /**
     * Logical name of the provider that failed, e.g. {@code stripe}. {@code null} when it could not be determined.
     */
    public String provider() {
        return provider;
    }

    /**
     * The provider's own error code, e.g. Stripe's {@code card_declined}, or {@code null} if it sent none. Diagnostic
     * only: it is never this service's {@code code}, because it is not from this service's catalogue.
     */
    public String providerCode() {
        return providerCode;
    }

    /**
     * HTTP status the provider returned, or {@code 0} if the call never produced a response. Deliberately never
     * becomes the status of our response — see the class javadoc.
     */
    public int providerStatus() {
        return providerStatus;
    }

    /**
     * Constructor reference of an {@link ExternalProviderException} subclass, so {@link Builder} can produce the named
     * type rather than this one.
     *
     * @param <T> the exception produced
     */
    @FunctionalInterface
    public interface Factory<T extends ExternalProviderException> {

        T create(ErrorPayload payload, Throwable cause, String provider, String providerCode, int providerStatus);

    }

    /**
     * Builder variant that also captures the provider's own reported code and status.
     *
     * @param <T> the exception produced
     */
    public static final class Builder<T extends ExternalProviderException> {

        private final ErrorBuilder<T> delegate;

        private String provider;

        private String providerCode;

        private int providerStatus;

        private Builder(ErrorCode errorCode, Factory<T> factory) {
            // The provider fields are read when build() runs, so they can still be set through the fluent methods
            // after this lambda is captured.
            this.delegate = new ErrorBuilder<>(errorCode,
                    (payload, cause) -> factory.create(payload, cause, provider, providerCode, providerStatus));
        }

        public Builder<T> detail(String value) {
            delegate.detail(value);
            return this;
        }

        public Builder<T> detail(String format, Object... args) {
            delegate.detail(format, args);
            return this;
        }

        public Builder<T> param(String name, Object value) {
            delegate.param(name, value);
            return this;
        }

        public Builder<T> params(Map<String, Object> values) {
            delegate.params(values);
            return this;
        }

        public Builder<T> fieldErrors(Collection<FieldError> values) {
            delegate.fieldErrors(values);
            return this;
        }

        public Builder<T> cause(Throwable value) {
            delegate.cause(value);
            return this;
        }

        public Builder<T> provider(String value) {
            this.provider = value;
            return this;
        }

        public Builder<T> providerCode(String value) {
            this.providerCode = value;
            return this;
        }

        public Builder<T> providerStatus(int value) {
            this.providerStatus = value;
            return this;
        }

        public T build() {
            return delegate.build();
        }

    }

}
