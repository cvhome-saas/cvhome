package com.asrevo.cvhome.content.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.Setter;

/**
 * How one store looks: its brand imagery, its social links and its site-level SEO copy.
 *
 * <p>
 * Deliberately not a {@link Content} row. It has no slug, no workflow, no revisions and no publish window, and it
 * is read on every layout render — putting it through the content table would buy nothing and cost a fake slug.
 * The translated SEO copy lives in the {@code seo} JSON column rather than a description table because it is never
 * queried by locale, which is the same reasoning behind {@code faq_group.names} and
 * {@code policy_version.translations}.
 * </p>
 */
@Entity
@Table(name = "SITE_SETTINGS")
@Getter
@Setter
public class SiteSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STORE_MERCHANT_ID", length = 50)
    private String storeMerchantId;

    @Column(name = "LOGO_MEDIA_ID")
    private Long logoMediaId;

    @Column(name = "LOGO_DARK_MEDIA_ID")
    private Long logoDarkMediaId;

    @Column(name = "FAVICON_MEDIA_ID")
    private Long faviconMediaId;

    @Column(name = "OG_MEDIA_ID")
    private Long ogMediaId;

    /** {@code {"metaTitle": {"en": "…"}, "metaDescription": {…}, "keywords": {…}}}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "SEO")
    private String seo;

    /** {@code [{"provider": "INSTAGRAM", "url": "…"}]}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "SOCIAL_LINKS")
    private String socialLinks;

    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

    @Column(name = "UPDATED_BY", length = 120)
    private String updatedBy;

}
