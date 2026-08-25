package com.asrevo.cvhome.billing.service.stripe;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.stripe.model.Event;

/**
 * Real Stripe event envelopes, loaded from {@code src/test/resources/stripe/}.
 *
 * <p>
 * Events are built by signing the fixture and running it through the same verifier the endpoint uses, rather than by
 * mocking {@link Event}. Two reasons. The handlers read the <em>raw JSON</em> out of
 * {@code getDataObjectDeserializer()} — deliberately, because Stripe has moved fields between API versions — and a
 * mocked event would let a test pass while that raw document was empty. And the fixtures are then exactly what
 * Stripe would deliver, so a payload shape that this service cannot actually read fails here rather than in
 * production.
 * </p>
 */
public final class StripeEventFixtures {

    /** Any secret will do: the fixture is signed and verified with the same one. */
    public static final String SIGNING_KEY = "whsec_fixture_key";

    private StripeEventFixtures() {
    }

    /** The raw body of a fixture, byte for byte as it will be signed. */
    public static String payload(String name) {
        String resource = String.format("/stripe/%s", name);
        try (InputStream in = StripeEventFixtures.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalArgumentException(String.format("No such Stripe fixture: %s", resource));
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The fixture as a verified {@link Event}, exactly as the webhook endpoint would hand it on. */
    public static Event event(String name) {
        String payload = payload(name);
        try {
            return com.stripe.net.Webhook.constructEvent(payload,
                    StripeSignatures.sign(payload, SIGNING_KEY), SIGNING_KEY);
        } catch (com.stripe.exception.SignatureVerificationException e) {
            throw new IllegalStateException("A fixture signed in this test failed to verify", e);
        }
    }

    /** The {@code data.object} of a fixture, which is what every apply handler is given. */
    public static String dataObject(String name) {
        return event(name).getDataObjectDeserializer().getRawJson();
    }

    /** The {@code data.object} parsed, for the readers that take JSON directly. */
    public static JsonObject dataJson(String name) {
        return StripeJson.parse(dataObject(name));
    }

}
