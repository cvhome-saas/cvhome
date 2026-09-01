package com.asrevo.cvhome.content.model.layout;

import java.util.List;

/**
 * One page's layout: the ordered sections the storefront renders, as a single document.
 *
 * <p>
 * Stored whole in {@code page_layout.draft} / {@code page_layout.published}. A layout is deliberately a document
 * and not rows: it is ordered, published atomically, undone as a whole and never queried per-field. The section
 * kinds, their variants and the shape of {@code props} are owned by the storefront's section catalogue
 * (landing-ui, {@code libs/theme/src/sections/catalog.ts}); this service validates structure and media
 * references, not per-kind prop shapes.
 * </p>
 */
public record LayoutDocument(Integer schemaVersion, PageKind page, List<LayoutSection> sections) {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    /** The most sections one page may hold; the same bound Shopify applies to a JSON template. */
    public static final int MAX_SECTIONS = 25;

    public List<LayoutSection> sections() {
        return sections == null ? List.of() : sections;
    }

}
