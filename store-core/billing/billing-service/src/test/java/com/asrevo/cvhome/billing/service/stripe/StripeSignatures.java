package com.asrevo.cvhome.billing.service.stripe;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Signs a payload the way Stripe does, so the verifier can be tested against a real signature rather than a mock.
 *
 * <p>
 * {@code Webhook.constructEvent} is static and stays static — it is a pure function of the payload, the header and
 * the secret and touches no network. Replacing it with {@code mockStatic} would prove only that the verifier calls
 * something; computing the HMAC here proves it verifies, which is the single piece of authentication a public
 * webhook endpoint has.
 * </p>
 *
 * <p>
 * The scheme is Stripe's: {@code t=<unix seconds>,v1=<hex HMAC-SHA256 of "<t>.<payload>">}.
 * </p>
 */
public final class StripeSignatures {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private StripeSignatures() {
    }

    /** A header Stripe would have sent for this payload, timestamped now. */
    public static String sign(String payload, String secret) {
        return sign(payload, secret, Instant.now());
    }

    /** A header timestamped at {@code at} — the way an expired signature is produced. */
    public static String sign(String payload, String secret, Instant at) {
        long timestamp = at.getEpochSecond();
        return String.format("t=%d,v1=%s", timestamp, hmacHex(secret, timestamp + "." + payload));
    }

    private static String hmacHex(String secret, String signedPayload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] digest = mac.doFinal(signedPayload.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

}
