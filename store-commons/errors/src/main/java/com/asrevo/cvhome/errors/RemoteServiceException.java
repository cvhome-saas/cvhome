package com.asrevo.cvhome.errors;

import java.io.Serial;
import java.util.Collection;
import java.util.Map;

/**
 * A call to <em>another cvhome service</em> failed. Carries what the remote service reported, so an error raised in
 * catalog still identifies itself as such after passing through checkout.
 *
 * <p>
 * Without this, a remote 400 surfaces to the browser as a local 500 carrying the remote service's stack text — which is
 * both wrong and unactionable. The web layer re-emits using {@link #remoteCode()}: a remote 4xx passes its status
 * through unchanged (a remote 404 stays a 404), while a remote 5xx becomes a 502, because a downstream fault is not the
 * caller's fault.
 * </p>
 *
 * <p>
 * Both of those re-emissions are only sound because the peer is one of ours: it speaks the same problem-detail
 * contract, so its {@code code} is a code from a catalogue we publish and its status means what our statuses mean. A
 * third party shares neither, which is why a failure from Stripe or PayPal is an {@link ExternalProviderException}
 * instead. Reaching for this type to describe a provider call re-creates exactly the two bugs that split them: the
 * provider's code overwrites the one a caller's {@code RemoteErrorCatalog} matches on, and the provider's status is
 * reflected back at an end user who had nothing to do with it.
 * </p>
 *
 * <p>
 * Abstract like every other category base. It was briefly concrete, on the argument that a remote service can fail in
 * ways this codebase has no type for — but "we have no name for this" is itself a nameable condition, and
 * {@link UnmappedRemoteFailureException} names it. Keeping the exemption would have left one generic type throwable
 * from anywhere, which is the single thing these rules exist to prevent.
 * </p>
 *
 * <p>
 * Subclass it with {@link #of(ErrorCode, Factory)}, which keeps the remote metadata while producing the named type.
 * </p>
 */
public abstract class RemoteServiceException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String remoteService;

    private final String remoteCode;

    private final int remoteStatus;

    protected RemoteServiceException(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode,
                                     int remoteStatus) {
        super(payload, cause);
        this.remoteService = remoteService;
        this.remoteCode = remoteCode;
        this.remoteStatus = remoteStatus;
    }

    /**
     * Builds a condition-named subclass — how a bounded context gets a descriptive exception (say
     * {@code PaymentInitiateRejectedException}) while keeping the remote metadata the web layer reads.
     */
    public static <T extends RemoteServiceException> Builder<T> of(ErrorCode errorCode, Factory<T> factory) {
        return new Builder<>(errorCode, factory);
    }

    /**
     * Logical name of the service that failed, e.g. {@code catalog}. {@code null} when it could not be determined.
     */
    public String remoteService() {
        return remoteService;
    }

    /**
     * The {@code code} from the remote service's problem body, or {@code null} if it did not send one — which is how
     * the web layer tells a coded remote failure from an opaque one.
     */
    public String remoteCode() {
        return remoteCode;
    }

    /**
     * HTTP status the remote service returned, or {@code 0} if the call never produced a response.
     */
    public int remoteStatus() {
        return remoteStatus;
    }

    /**
     * Constructor reference of a {@link RemoteServiceException} subclass, so {@link Builder} can produce the named
     * type rather than this one.
     *
     * @param <T> the exception produced
     */
    @FunctionalInterface
    public interface Factory<T extends RemoteServiceException> {

        T create(ErrorPayload payload, Throwable cause, String remoteService, String remoteCode, int remoteStatus);

    }

    /**
     * Builder variant that also captures the remote service's own reported code and status.
     *
     * @param <T> the exception produced
     */
    public static final class Builder<T extends RemoteServiceException> {

        private final ErrorBuilder<T> delegate;

        private String remoteService;

        private String remoteCode;

        private int remoteStatus;

        private Builder(ErrorCode errorCode, Factory<T> factory) {
            // The remote fields are read when build() runs, so they can still be set through the fluent methods after
            // this lambda is captured.
            this.delegate = new ErrorBuilder<>(errorCode,
                    (payload, cause) -> factory.create(payload, cause, remoteService, remoteCode, remoteStatus));
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

        /**
         * Bulk form, used when the context being copied already arrived as a map — chiefly the {@code params} a remote
         * service sent in its problem body.
         */
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

        public Builder<T> remoteService(String value) {
            this.remoteService = value;
            return this;
        }

        public Builder<T> remoteCode(String value) {
            this.remoteCode = value;
            return this;
        }

        public Builder<T> remoteStatus(int value) {
            this.remoteStatus = value;
            return this;
        }

        public T build() {
            return delegate.build();
        }

    }

}
