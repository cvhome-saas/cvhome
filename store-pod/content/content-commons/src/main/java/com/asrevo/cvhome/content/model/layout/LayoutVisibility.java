package com.asrevo.cvhome.content.model.layout;

import java.util.List;

/**
 * Whether a section shows at all ({@code hidden} keeps it in the document but off the page) and on which device
 * classes ({@code desktop} / {@code tablet} / {@code mobile}; null or empty means all).
 */
public record LayoutVisibility(Boolean hidden, List<String> devices) {

    public boolean isHidden() {
        return Boolean.TRUE.equals(hidden);
    }

}
