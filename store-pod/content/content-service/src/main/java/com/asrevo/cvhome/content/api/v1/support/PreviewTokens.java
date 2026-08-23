package com.asrevo.cvhome.content.api.v1.support;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Short-lived HMAC tokens that let the storefront render an unpublished page or post for the editor's preview.
 * {@code <store>.<slug>.<expiresEpochSeconds>.<hmac>}, signed with a per-process key (a restart invalidates
 * outstanding previews, which is acceptable for a 30-minute token).
 */
@Component
public class PreviewTokens {

    private static final Duration TTL = Duration.ofMinutes(30);

    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] key = new byte[32];

    private final Clock clock;

    public PreviewTokens(Clock clock) {
        this.clock = clock;
        new SecureRandom().nextBytes(key);
    }

    public String issue(StoreMerchantId store, String slug) {
        long expires = clock.instant().plus(TTL).getEpochSecond();
        String payload = String.format("%s.%s.%d", store.getId(), slug, expires);
        return String.format("%s.%s", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)), sign(payload));
    }

    public boolean valid(String token, StoreMerchantId store, String slug) {
        if (token == null || token.isBlank()) {
            return false;
        }
        int dot = token.lastIndexOf('.');
        if (dot <= 0) {
            return false;
        }
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(token.substring(0, dot)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException _) {
            return false;
        }
        if (!sign(payload).equals(token.substring(dot + 1))) {
            return false;
        }
        String[] parts = payload.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        return parts[0].equals(store.getId()) && parts[1].equals(slug)
                && Long.parseLong(parts[2]) >= clock.instant().getEpochSecond();
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key, ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

}
