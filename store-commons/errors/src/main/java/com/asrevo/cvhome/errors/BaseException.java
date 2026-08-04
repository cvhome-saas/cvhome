package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * Checked root of every application failure. Abstract on purpose — see the rules below.
 *
 * <p>
 * Checked by deliberate choice: a method's signature states which failures it can produce, so a caller cannot ignore
 * one by accident and the compiler flags a new failure mode at every call site. Where the Java type system gets in the
 * way — most often inside a lambda passed to {@code stream().map(...)}, since {@code Function} cannot throw a checked
 * exception — wrap with {@link Unchecked}; the web layer unwraps the carrier transparently.
 * </p>
 *
 * <h2>Rules</h2>
 *
 * <ol>
 * <li><b>Never throw a generic type.</b> Not this class, and not a category base ({@link ValidationException},
 * {@link ResourceNotFoundException}, {@link ConversionException}, …). Throw a class whose <em>name</em> states the
 * condition: {@code PriceNotParseableException}, {@code InvalidWebhookSignatureException},
 * {@code DuplicateSkuException}. Those bases are abstract precisely so the compiler enforces this rather than a review
 * comment.</li>
 *
 * <li><b>Never declare a generic type.</b> {@code throws BaseException} tells a caller only "something may fail",
 * which it already knew. Declare the exact exceptions a method produces, so the reader learns the failure modes from
 * the signature and a newly added one breaks the build at every site that has to decide what it means.</li>
 *
 * <li><b>The condition names the class; the category names the parent.</b> A named exception extends the category base
 * that fixes its HTTP status — that is the base's only job. {@code NonPositivePriceException extends
 * ValidationException} is a 400 because validation failures are, and nothing at the throw site restates it.</li>
 *
 * <li><b>Attaching a better {@link ErrorCode} to a generic exception is not a migration.</b> It improves the response
 * body while leaving the signature — the part a caller reads — saying nothing. Define the type.</li>
 *
 * <li><b>One class per condition, with a static factory that names its inputs.</b>
 * {@code PriceNotParseableException.of(amount, cause)} beats a builder chain repeated at every site: the params a
 * support engineer will search on are then guaranteed rather than remembered.</li>
 *
 * <li><b>Catch narrowly.</b> Catch the named types, or a category base when the handling genuinely is per-category.
 * Catching this type to branch on {@link ErrorCodeAware#category()} re-creates at runtime the distinction the type
 * system was already making for free.</li>
 * </ol>
 *
 * <h2>Failures that happened elsewhere</h2>
 *
 * <p>
 * Two category bases describe a failure this service only relayed, and they are not interchangeable.
 * {@link RemoteServiceException} means another cvhome service failed: it speaks our problem-detail contract, so its
 * code and status are re-emitted as they stand. {@link ExternalProviderException} means a third party failed — Stripe,
 * PayPal, a carrier — and its code and status are diagnostic extensions only, because they belong to somebody else's
 * vocabulary. Which one a throw site picks decides what a caller sees, so pick by <em>who</em> failed, never by which
 * import is already present.
 * </p>
 */
public abstract class BaseException extends Exception implements ErrorCodeAware {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ErrorPayload payload;

    protected BaseException(ErrorPayload payload, Throwable cause) {
        super(payload.toMessage(), cause);
        this.payload = payload;
    }

    @Override
    public ErrorPayload payload() {
        return payload;
    }

}
