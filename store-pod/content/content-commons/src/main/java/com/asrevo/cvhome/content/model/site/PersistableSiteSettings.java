package com.asrevo.cvhome.content.model.site;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.commons.domain.SocialLink;

import lombok.Getter;
import lombok.Setter;

/**
 * What the console writes on the store's appearance record. Media slots are asset ids; the store's own SEO copy
 * is per locale.
 *
 * <p>
 * Sending {@code null} for a media slot clears it — which is how a logo finally becomes removable. Merchant, which
 * used to own these, only ever had upload endpoints.
 * </p>
 */
@Getter
@Setter
public class PersistableSiteSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long logoMediaId;

    private Long logoDarkMediaId;

    private Long faviconMediaId;

    private Long ogMediaId;

    /**
     * Per-field, per-locale SEO copy: {@code {"metaTitle": {"en": "…", "ar": "…"}, "metaDescription": {…}}}.
     * Held as a map rather than a description table because site SEO is never queried by locale, which is the
     * same reasoning behind {@code faq_group.names} and {@code policy_version.translations}.
     */
    private Map<String, Map<String, String>> seo = new LinkedHashMap<>();

    private List<SocialLink> socialLinks = List.of();

}
