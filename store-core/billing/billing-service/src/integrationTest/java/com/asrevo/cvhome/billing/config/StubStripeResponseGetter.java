package com.asrevo.cvhome.billing.config;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.net.ApiMode;
import com.stripe.net.ApiResource;
import com.stripe.net.BaseAddress;
import com.stripe.net.RequestOptions;
import com.stripe.net.StripeResponseGetter;

/**
 * Stripe's SDK with the network taken out, at the seam {@code StripeClient} already provides.
 *
 * <p>
 * Responses are canned JSON deserialised by Stripe's own {@code ApiResource.GSON}, so the gateways receive real
 * model objects assembled the way the SDK assembles them — not mocks whose getters were told what to say. That
 * matters here because the gateways read nested structure out of those models ({@code subscription.getItems()
 * .getData().getFirst().getId()}), and a mock would let that navigation be wrong without anything noticing.
 * </p>
 *
 * <p>
 * Failures are queued rather than configured per path, because what these tests are about is <em>which</em>
 * exception a gateway turns a Stripe failure into: a {@link CardException} is Stripe refusing — a decision, and
 * the caller may fail the change — while any other {@link StripeException} means no answer arrived and the
 * outcome is unknown. Collapsing those two is the failure mode this stub exists to make visible.
 * </p>
 */
public final class StubStripeResponseGetter implements StripeResponseGetter {

    private final Deque<Object> answers = new ArrayDeque<>();

    private String lastPath;

    private String lastIdempotencyKey;

    /** Answers the next call with this JSON, deserialised into whatever type the caller asked for. */
    public StubStripeResponseGetter thenJson(String json) {
        answers.add(json);
        return this;
    }

    /** Answers the next call by refusing it the way Stripe refuses a card. */
    public StubStripeResponseGetter thenDeclined(String code) {
        answers.add(new CardException("Your card was declined.", null, code, null, null, null, 402, null));
        return this;
    }

    /** Answers the next call the way an unreachable Stripe does: no status, no decision. */
    public StubStripeResponseGetter thenUnreachable() {
        answers.add(new ApiConnectionException("Could not reach Stripe."));
        return this;
    }

    public String lastPath() {
        return lastPath;
    }

    /** The idempotency key the gateway sent, which is the only evidence a retry would be recognised. */
    public String lastIdempotencyKey() {
        return lastIdempotencyKey;
    }

    public void reset() {
        answers.clear();
        lastPath = null;
        lastIdempotencyKey = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StripeObject> T request(BaseAddress baseAddress, ApiResource.RequestMethod method, String path,
                                              Map<String, Object> params, Type typeToken, RequestOptions options,
                                              ApiMode apiMode) throws StripeException {
        lastPath = path;
        lastIdempotencyKey = options == null ? null : options.getIdempotencyKey();
        Object next = answers.poll();
        if (next instanceof StripeException failure) {
            throw failure;
        }
        String json = next instanceof String canned ? canned : "{}";
        return (T) ApiResource.GSON.fromJson(json, typeToken);
    }

    @Override
    public InputStream requestStream(BaseAddress baseAddress, ApiResource.RequestMethod method, String path,
                                     Map<String, Object> params, RequestOptions options, ApiMode apiMode)
            throws StripeException {
        throw new ApiConnectionException("No gateway streams from Stripe.");
    }

}
