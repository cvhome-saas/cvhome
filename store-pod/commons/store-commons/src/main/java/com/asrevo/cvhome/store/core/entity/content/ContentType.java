package com.asrevo.cvhome.store.core.entity.content;

/**
 * Kind of a CMS content row. Every kind shares one table and one workflow.
 *
 * <p>
 * {@code BOX} used to sit here too, holding the store-level "snippets" ({@code meta-title},
 * {@code header-message}, {@code agreement}, {@code LANDING_PAGE}). Those were a parallel, workflow-less way to
 * say things the real components already say better, so each moved to the component that supersedes it — site
 * SEO to the appearance record, the announcement to a STRIP banner, the agreement to the live TERMS policy, and
 * the landing copy to {@code SECTION} rows.
 * </p>
 */
public enum ContentType {

    PAGE, SECTION, POST, BANNER, FAQ, POLICY

}
