package com.asrevo.cvhome.catalog.reads;

import java.util.Locale;

import com.asrevo.cvhome.commons.domain.KeyPart;

/**
 * What a shopper typed, as the suggest read keys it: lowered, trimmed and cut at {@link #MAX_LENGTH}, with the
 * limit clamped to what the search would cap it to anyway. {@code Sho}, {@code sho } and {@code sho} share one
 * entry, and a raw query would key an entry per keystroke.
 *
 * @param text  the normalised text, possibly empty
 * @param limit the clamped limit
 */
public record SuggestQuery(String text, int limit) implements KeyPart {

    /** Longer than any product name a shopper types before the suggestions have answered. */
    public static final int MAX_LENGTH = 64;

    public static final int MAX_SUGGESTIONS = 10;

    public static SuggestQuery of(String raw, int limit) {
        String typed = raw == null ? "" : raw.strip().toLowerCase(Locale.ROOT);
        return new SuggestQuery(typed.length() > MAX_LENGTH ? typed.substring(0, MAX_LENGTH) : typed,
                Math.clamp(limit, 1, MAX_SUGGESTIONS));
    }

    @Override
    public String cacheKeyPart() {
        return String.format("%s:%d", text, limit);
    }
}
