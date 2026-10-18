package com.asrevo.cvhome.content.reads;

import com.asrevo.cvhome.commons.domain.KeyPart;

/**
 * The blog list's two optional filters as one key part: a category slug and a tag, either or both absent.
 *
 * @param category the category slug, or {@code null}
 * @param tag      the tag, or {@code null}
 */
public record PostsFilter(String category, String tag) implements KeyPart {

    private static final String NONE = "-";

    public static PostsFilter of(String category, String tag) {
        return new PostsFilter(blankToNull(category), blankToNull(tag));
    }

    @Override
    public String cacheKeyPart() {
        return String.format("%s:%s", category == null ? NONE : category, tag == null ? NONE : tag);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}
