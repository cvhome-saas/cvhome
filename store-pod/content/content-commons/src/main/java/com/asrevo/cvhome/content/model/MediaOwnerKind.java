package com.asrevo.cvhome.content.model;

/**
 * What holds a reference to a media asset.
 *
 * <p>
 * Orthogonal to {@link com.asrevo.cvhome.store.core.entity.content.ContentType}, not a replacement for it: a
 * content-owned row carries {@code CONTENT} here <em>and</em> its own content type, so the console can still say
 * "used by the page <em>About us</em>". The other kinds exist because appearance and catalogue data live outside
 * the {@code content} table — {@code SITE_SETTINGS} for the store's logo and favicon, and {@code PRODUCT} /
 * {@code CATEGORY} / {@code BRAND} for rows owned by the catalog service, which registers them over
 * {@code ExternalMediaService}.
 * </p>
 */
public enum MediaOwnerKind {

    CONTENT, SITE_SETTINGS, PRODUCT, CATEGORY, BRAND, LAYOUT;

    /**
     * Whether rows of this kind live in this service's own {@code content} table, so a title can be resolved
     * locally instead of being taken from what the caller supplied.
     */
    public boolean local() {
        return this == CONTENT;
    }

}
