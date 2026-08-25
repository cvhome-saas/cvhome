package com.asrevo.cvhome.content;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

/**
 * Shared builders for the content unit tests. Entities here are plain objects — no persistence context, no
 * Spring — so every test can shape exactly the row it is about.
 */
public final class ContentFixtures {

    public static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    public static final StoreMerchantId OTHER_STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    public static final LanguageCode EN = new LanguageCode("en");

    public static final LanguageCode AR = new LanguageCode("ar");

    public static final Instant NOW = Instant.parse("2026-03-01T12:00:00Z");

    private ContentFixtures() {
    }

    public static Clock clock() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }

    public static Content content(Long id, ContentType type, String code) {
        Content c = new Content();
        c.setId(id);
        c.setStoreMerchantId(STORE);
        c.setContentType(type);
        c.setCode(code);
        c.setStatus(ContentStatus.DRAFT);
        c.setVersion(0);
        c.setAuditSection(new AuditSection());
        return c;
    }

    /**
     * A published, servable row of {@code type} with one complete English locale.
     */
    public static Content published(Long id, ContentType type, String code, String title) {
        Content c = content(id, type, code);
        c.setStatus(ContentStatus.PUBLISHED);
        c.setVisible(true);
        c.setPublishAt(NOW.minusSeconds(3600));
        c.getDescriptions().add(description(c, EN, title, "<p>body</p>"));
        return c;
    }

    public static ContentDescription description(Content owner, LanguageCode language, String name, String body) {
        ContentDescription d = new ContentDescription();
        d.setContent(owner);
        d.setLanguageCode(language);
        d.setName(name);
        d.setTitle(name);
        d.setDescription(body);
        d.setSeUrl(owner == null ? name : owner.getCode());
        d.setState(TranslationState.TRANSLATED);
        return d;
    }

    public static ContentTranslation translation(LanguageCode language, String title, String body) {
        ContentTranslation t = new ContentTranslation();
        t.setLanguage(language);
        t.setTitle(title);
        t.setBody(body);
        return t;
    }

}
