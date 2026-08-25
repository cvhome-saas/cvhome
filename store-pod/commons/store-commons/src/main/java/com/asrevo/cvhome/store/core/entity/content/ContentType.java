package com.asrevo.cvhome.store.core.entity.content;

/**
 * Kind of a CMS content row. The first three are the legacy kinds ({@code BOX} rows are the store-level "snippets"
 * such as {@code meta-title} and {@code header-message}); the rest were added with the content platform and share
 * the same table.
 */
public enum ContentType {

    BOX, PAGE, SECTION, POST, BANNER, FAQ, POLICY;

    /**
     * Whether rows of this type have a status workflow and appear in the console lists. Legacy {@code BOX} and
     * {@code SECTION} rows do not — they are always live.
     */
    public boolean workflow() {
        return this != BOX && this != SECTION;
    }

}
