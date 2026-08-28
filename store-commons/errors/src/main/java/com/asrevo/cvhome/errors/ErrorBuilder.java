package com.asrevo.cvhome.errors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Fluent builder shared by every exception type, so adding an exception costs one static {@code of} factory rather
 * than a builder class of its own.
 *
 * <pre>{@code
 * throw ResourceNotFoundException.of(CatalogErrors.PRODUCT_NOT_FOUND)
 *         .param("productId", id)
 *         .param("storeId", storeId)
 *         .build();
 * }</pre>
 *
 * <p>
 * The constructor is public so a bounded context can define its own condition-named exceptions in its own module —
 * {@code new ErrorBuilder<>(PaymentErrors.WEBHOOK_SIGNATURE_INVALID, InvalidWebhookSignatureException::new)} — without
 * this module having to know about them.
 * </p>
 *
 * @param <T> the exception this builder produces
 */
public final class ErrorBuilder<T extends Throwable> {

    private final ErrorCode errorCode;

    private final BiFunction<ErrorPayload, Throwable, T> factory;

    private final Map<String, Object> params = new LinkedHashMap<>();

    private final List<FieldError> fieldErrors = new ArrayList<>();

    private String detail;

    private Throwable cause;

    public ErrorBuilder(ErrorCode errorCode, BiFunction<ErrorPayload, Throwable, T> factory) {
        this.errorCode = errorCode;
        this.factory = factory;
    }

    /**
     * Human-readable elaboration for logs and for clients that have no translation for the code. Never put secrets or
     * internal stack text here — it can reach the client.
     */
    public ErrorBuilder<T> detail(String value) {
        this.detail = value;
        return this;
    }

    /**
     * Formatted variant of {@link #detail(String)}, using {@link String#format(String, Object...)}.
     */
    public ErrorBuilder<T> detail(String format, Object... args) {
        this.detail = String.format(format, args);
        return this;
    }

    /**
     * Structured context a client can render or a support engineer can search on, e.g. {@code param("productId", id)}.
     */
    public ErrorBuilder<T> param(String name, Object value) {
        this.params.put(name, value);
        return this;
    }

    public ErrorBuilder<T> params(Map<String, Object> values) {
        if (values != null) {
            this.params.putAll(values);
        }
        return this;
    }

    public ErrorBuilder<T> fieldError(FieldError fieldError) {
        this.fieldErrors.add(fieldError);
        return this;
    }

    public ErrorBuilder<T> fieldError(String field, ErrorCode code, String message) {
        return fieldError(FieldError.of(field, code, message));
    }

    public ErrorBuilder<T> fieldErrors(Collection<FieldError> values) {
        if (values != null) {
            this.fieldErrors.addAll(values);
        }
        return this;
    }

    /**
     * The underlying failure. It is logged with the stack trace but never rendered into the client response.
     */
    public ErrorBuilder<T> cause(Throwable value) {
        this.cause = value;
        return this;
    }

    public T build() {
        return factory.apply(new ErrorPayload(errorCode, detail, params, fieldErrors), cause);
    }

}
