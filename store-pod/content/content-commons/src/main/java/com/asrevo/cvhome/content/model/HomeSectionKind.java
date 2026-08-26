package com.asrevo.cvhome.content.model;

/**
 * What a home-page section renders.
 *
 * <p>
 * Before this existed the storefront's home page was a hard-coded list of four product groups in the Next.js
 * loader, so a seller could not reorder it, retitle it or put anything else on the page. Each kind here maps to a
 * block the themes already know how to draw.
 * </p>
 */
public enum HomeSectionKind {

    /** A catalog product group; {@code targetValue} is the group code. */
    PRODUCT_GROUP,
    /** A grid of child categories; {@code targetValue} is the parent category code. */
    CATEGORY_GRID,
    /** An existing banner, by slug, so artwork is authored once. */
    BANNER_REF,
    /** Free copy from the section's own translations. */
    RICH_TEXT,
    /** A single image from the media library, with the section's copy over it. */
    IMAGE,
    /** The most recent blog posts. */
    POST_FEED,
    /** An FAQ group; {@code targetValue} is the group key. */
    FAQ_REF;

    /**
     * Whether this kind is meaningless without a {@code targetValue} — checked when publishing.
     */
    public boolean needsTarget() {
        return this == PRODUCT_GROUP || this == CATEGORY_GRID || this == BANNER_REF || this == FAQ_REF;
    }

}
