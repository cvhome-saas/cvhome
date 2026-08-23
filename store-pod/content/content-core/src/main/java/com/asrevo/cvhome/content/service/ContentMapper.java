package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.ContentAudit;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.common.LocaleState;
import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.content.model.common.ReadableContentRow;
import com.asrevo.cvhome.content.support.HtmlSanitizer;
import com.asrevo.cvhome.content.support.Strings;

/**
 * The common half of entity ⇄ DTO, shared by every type. Translations are applied as a whole: rows for locales not
 * in the request are removed ({@code orphanRemoval}), rows present are updated in place so their ids survive.
 */
public final class ContentMapper {

    private ContentMapper() {
    }

    public static void applyCommon(Content entity, PersistableContent dto) {
        entity.setCode(dto.getSlug());
        entity.setPublishAt(dto.getPublishAt());
        entity.setUnpublishAt(dto.getUnpublishAt());
        entity.setNoindex(dto.isNoindex());
        entity.setCanonicalUrl(Strings.trimToNull(dto.getCanonicalUrl()));
        entity.setOgMediaId(dto.getOgMediaId());
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }
    }

    /**
     * @return whether the body of {@code sourceLocale} changed — the trigger for marking other locales stale
     */
    public static boolean applyTranslations(Content entity, List<ContentTranslation> translations,
                                            LanguageCode sourceLocale, boolean requiresBody) {
        Map<LanguageCode, ContentDescription> existing = new LinkedHashMap<>();
        for (ContentDescription d : entity.getDescriptions()) {
            existing.put(d.getLanguageCode(), d);
        }
        List<ContentDescription> next = new ArrayList<>();
        boolean sourceBodyChanged = false;
        for (ContentTranslation t : translations) {
            if (t.getLanguage() == null || !t.getLanguage().isLanguage()) {
                continue;
            }
            boolean empty = Strings.blank(t.getTitle()) && Strings.blank(t.getBody());
            if (empty) {
                continue; // an empty locale is "missing", not a row
            }
            ContentDescription d = existing.getOrDefault(t.getLanguage(), new ContentDescription());
            String body = HtmlSanitizer.clean(t.getBody());
            if (d.getId() != null && t.getLanguage().equals(sourceLocale)
                    && !Objects.equals(d.getDescription(), body)) {
                sourceBodyChanged = true;
            }
            d.setContent(entity);
            d.setLanguageCode(t.getLanguage());
            d.setName(Strings.blank(t.getTitle()) ? entity.getCode() : Strings.abbreviate(t.getTitle().trim(), 120));
            d.setTitle(Strings.abbreviate(Strings.trimToNull(t.getTitle()), 100));
            d.setDescription(body);
            d.setExcerpt(Strings.trimToNull(t.getExcerpt()));
            d.setSeUrl(Strings.blank(t.getFriendlyUrl()) ? entity.getCode() : t.getFriendlyUrl().trim());
            d.setMetatagTitle(Strings.trimToNull(t.getMetaTitle()));
            d.setMetatagDescription(Strings.trimToNull(t.getMetaDescription()));
            d.setMetatagKeywords(Strings.trimToNull(t.getKeywords()));
            d.setAltText(Strings.trimToNull(t.getAltText()));
            d.setCtaLabel(Strings.trimToNull(t.getCtaLabel()));
            d.setSubtitle(Strings.trimToNull(t.getSubtitle()));
            boolean complete = !Strings.blank(t.getTitle()) && (!requiresBody || !Strings.blank(t.getBody()));
            if (t.getState() == TranslationState.STALE && complete) {
                d.setState(TranslationState.STALE);
            } else {
                d.setState(complete ? TranslationState.TRANSLATED : TranslationState.DRAFT);
            }
            next.add(d);
        }
        entity.getDescriptions().clear();
        entity.getDescriptions().addAll(next);
        return sourceBodyChanged;
    }

    public static void populateCommon(Content entity, PersistableContent dto) {
        dto.setId(entity.getId());
        dto.setVersion(entity.getVersion());
        dto.setSlug(entity.getCode());
        dto.setPublishAt(entity.getPublishAt());
        dto.setUnpublishAt(entity.getUnpublishAt());
        dto.setNoindex(entity.isNoindex());
        dto.setCanonicalUrl(entity.getCanonicalUrl());
        dto.setOgMediaId(entity.getOgMediaId());
        dto.setSortOrder(entity.getSortOrder());
        dto.setTranslations(translations(entity));
    }

    public static List<ContentTranslation> translations(Content entity) {
        List<ContentTranslation> out = new ArrayList<>();
        for (ContentDescription d : entity.getDescriptions()) {
            out.add(translation(d));
        }
        out.sort((a, b) -> a.getLanguage().compareTo(b.getLanguage()));
        return out;
    }

    public static ContentTranslation translation(ContentDescription d) {
        ContentTranslation t = new ContentTranslation();
        t.setId(d.getId());
        t.setLanguage(d.getLanguageCode());
        t.setState(d.getState());
        t.setTitle(d.getTitle() != null ? d.getTitle() : d.getName());
        t.setBody(d.getDescription());
        t.setExcerpt(d.getExcerpt());
        t.setFriendlyUrl(d.getSeUrl());
        t.setMetaTitle(d.getMetatagTitle());
        t.setMetaDescription(d.getMetatagDescription());
        t.setKeywords(d.getMetatagKeywords());
        t.setAltText(d.getAltText());
        t.setCtaLabel(d.getCtaLabel());
        t.setSubtitle(d.getSubtitle());
        return t;
    }

    public static List<LocaleState> locales(Content entity) {
        List<LocaleState> out = new ArrayList<>();
        for (ContentDescription d : entity.getDescriptions()) {
            out.add(new LocaleState(d.getLanguageCode().code(), d.getState()));
        }
        out.sort((a, b) -> a.getCode().compareTo(b.getCode()));
        return out;
    }

    public static ContentAudit audit(Content entity) {
        ContentAudit a = new ContentAudit();
        if (entity.getAuditSection() != null) {
            a.setCreatedAt(entity.getAuditSection().getDateCreated());
            a.setUpdatedAt(entity.getAuditSection().getDateModified());
        }
        a.setCreatedBy(entity.getCreatedBy());
        a.setUpdatedBy(entity.getUpdatedBy());
        return a;
    }

    /**
     * The title in {@code language}, falling back to the first locale, then the slug.
     */
    public static String title(Content entity, LanguageCode language) {
        ContentDescription d = entity.description(language).orElseGet(entity::getDescription);
        if (d == null) {
            return entity.getCode();
        }
        return d.getTitle() != null ? d.getTitle() : d.getName();
    }

    public static ReadableContentRow row(Content entity, LanguageCode language, String subtitle) {
        ReadableContentRow r = new ReadableContentRow();
        r.setId(entity.getId());
        r.setType(entity.getContentType());
        r.setSlug(entity.getCode());
        r.setTitle(title(entity, language));
        r.setSubtitle(subtitle != null ? subtitle : String.format("/%s", entity.getCode()));
        r.setStatus(entity.getStatus());
        r.setPublishAt(entity.getPublishAt());
        r.setLocales(locales(entity));
        if (entity.getAuditSection() != null) {
            r.setUpdatedAt(entity.getAuditSection().getDateModified());
        }
        r.setUpdatedBy(entity.getUpdatedBy());
        return r;
    }

}
