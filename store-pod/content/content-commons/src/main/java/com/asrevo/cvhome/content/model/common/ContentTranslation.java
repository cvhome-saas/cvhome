package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

/**
 * One locale of a content item. Maps 1:1 onto a {@code content_description} row: {@code title} is the row's
 * {@code name}, {@code body} its {@code description}, {@code friendlyUrl} its {@code sef_url}.
 *
 * <p>
 * Which fields matter depends on the item type: pages and posts use title/body/SEO; banners use title (headline),
 * subtitle, ctaLabel and altText; FAQ entries use title (question) and body (answer); policies use title (heading)
 * and body.
 * </p>
 */
@Getter
@Setter
public class ContentTranslation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull
    @JsonSerialize(using = LanguageCodeSerializer.class)
    @JsonDeserialize(using = LanguageCodeDeSerializer.class)
    private LanguageCode language;

    private TranslationState state;

    @Size(max = 120)
    private String title;

    private String body;

    @Size(max = 300)
    private String excerpt;

    @Size(max = 120)
    private String friendlyUrl;

    @Size(max = 255)
    private String metaTitle;

    @Size(max = 255)
    private String metaDescription;

    @Size(max = 255)
    private String keywords;

    @Size(max = 255)
    private String altText;

    @Size(max = 60)
    private String ctaLabel;

    @Size(max = 300)
    private String subtitle;

}
