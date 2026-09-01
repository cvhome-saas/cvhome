package com.asrevo.cvhome.content.model.layout;

import java.util.List;
import java.util.Map;

/**
 * One block on the page.
 *
 * <p>
 * {@code id} is a client-generated stable string (drag keys, canvas selection, {@code data-section-id} in the
 * storefront DOM). {@code kind} and {@code variant} pick the renderer; {@code props} are the kind's machine
 * settings, where entity references are business codes (product-group code, category code, faq slug) and media
 * references are the media library's asset ids under keys named {@code mediaId}. {@code text} holds the
 * page-owned localized copy as {@code {field: {locale: value}}} — the same locale-map shape as
 * {@code menu.names}, because layout copy is never queried by locale.
 * </p>
 */
public record LayoutSection(String id, String kind, String variant, Map<String, Object> props,
                            List<LayoutItem> items, Map<String, Map<String, String>> text, LayoutStyle style,
                            LayoutVisibility visibility, String anchor, Boolean locked) {

    /** The most items one section may hold; the same bound Shopify applies to a section's blocks. */
    public static final int MAX_ITEMS = 50;

    public List<LayoutItem> items() {
        return items == null ? List.of() : items;
    }

    public Map<String, Object> props() {
        return props == null ? Map.of() : props;
    }

    public Map<String, Map<String, String>> text() {
        return text == null ? Map.of() : text;
    }

}
