package com.asrevo.cvhome.content.service;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.common.ReadableContentRow;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The common half of entity ⇄ DTO. The rules that matter here: the translations list is authoritative (a locale
 * left out is deleted), an empty locale is "missing" rather than an empty row, and only a real body change in the
 * source locale is allowed to report back as changed — that is what marks the other locales stale.
 */
class ContentMapperTest {

    private static final String ARABIC_TITLE = "عن";

    private static final String SEO_TITLE = "About us | Example";

    private static final String ACTOR = "ada";

    private static final String EDITOR = "bob";

    private static final String GROUPED_SUBTITLE = "general · #1";

    private static final String SLUG = "about-us";

    private static final String BODY = "<p>hello</p>";

    private static final String TITLE = "About us";

    @Test
    void applyCommonCopiesTheSeoAndScheduleFields() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        PersistablePage dto = new PersistablePage();
        dto.setSlug(SLUG);
        dto.setPublishAt(ContentFixtures.NOW);
        dto.setUnpublishAt(ContentFixtures.NOW.plusSeconds(60));
        dto.setNoindex(true);
        dto.setCanonicalUrl("  https://example.test/about  ");
        dto.setOgMediaId(7L);
        dto.setSortOrder(4);

        ContentMapper.applyCommon(entity, dto);

        assertThat(entity.getCode()).isEqualTo(SLUG);
        assertThat(entity.getPublishAt()).isEqualTo(ContentFixtures.NOW);
        assertThat(entity.getUnpublishAt()).isEqualTo(ContentFixtures.NOW.plusSeconds(60));
        assertThat(entity.isNoindex()).isTrue();
        assertThat(entity.getCanonicalUrl()).isEqualTo("https://example.test/about");
        assertThat(entity.getOgMediaId()).isEqualTo(7L);
        assertThat(entity.getSortOrder()).isEqualTo(4);
    }

    @Test
    void nullSortOrderLeavesTheStoredOneAlone() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        entity.setSortOrder(9);
        PersistablePage dto = new PersistablePage();
        dto.setSlug(SLUG);

        ContentMapper.applyCommon(entity, dto);

        assertThat(entity.getSortOrder()).isEqualTo(9);
    }

    @Test
    void anEmptyLocaleIsMissingRatherThanAnEmptyRow() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        ContentMapper.applyTranslations(entity, List.of(
                ContentFixtures.translation(ContentFixtures.EN, TITLE, BODY),
                ContentFixtures.translation(ContentFixtures.AR, "  ", null)), null, true);

        assertThat(entity.getDescriptions()).singleElement()
                .extracting(ContentDescription::getLanguageCode).isEqualTo(ContentFixtures.EN);
    }

    @Test
    void aTranslationWithoutAUsableLanguageIsSkipped() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        ContentMapper.applyTranslations(entity, List.of(
                ContentFixtures.translation(null, TITLE, BODY),
                ContentFixtures.translation(new LanguageCode("e"), TITLE, BODY)), null, true);

        assertThat(entity.getDescriptions()).isEmpty();
    }

    @Test
    void aLocaleLeftOutOfTheListIsRemoved() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        entity.getDescriptions().add(ContentFixtures.description(entity, ContentFixtures.AR, ARABIC_TITLE, BODY));

        ContentMapper.applyTranslations(entity,
                List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, BODY)), null, true);

        assertThat(entity.getDescriptions()).singleElement()
                .extracting(ContentDescription::getLanguageCode).isEqualTo(ContentFixtures.EN);
    }

    @Test
    void onlyABodyChangeInTheSourceLocaleReportsBack() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentDescription existing = ContentFixtures.description(entity, ContentFixtures.EN, TITLE, BODY);
        existing.setId(10L);
        entity.getDescriptions().add(existing);

        boolean unchanged = ContentMapper.applyTranslations(entity,
                List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, BODY)), ContentFixtures.EN, true);
        boolean changed = ContentMapper.applyTranslations(entity,
                List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, "<p>rewritten</p>")),
                ContentFixtures.EN, true);

        assertThat(unchanged).isFalse();
        assertThat(changed).isTrue();
    }

    @Test
    void aBrandNewRowNeverCountsAsAChangedSource() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        boolean changed = ContentMapper.applyTranslations(entity,
                List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, BODY)), ContentFixtures.EN, true);

        assertThat(changed).isFalse();
    }

    @Test
    void bodyIsSanitisedOnWrite() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        ContentMapper.applyTranslations(entity, List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE,
                "<p>ok</p><script>alert(1)</script>")), null, true);

        assertThat(entity.getDescriptions().getFirst().getDescription()).doesNotContain("script");
    }

    @Test
    void aMissingTitleFallsBackToTheSlugAndTheFriendlyUrlToo() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        ContentMapper.applyTranslations(entity,
                List.of(ContentFixtures.translation(ContentFixtures.EN, null, BODY)), null, true);

        ContentDescription d = entity.getDescriptions().getFirst();
        assertThat(d.getName()).isEqualTo(SLUG);
        assertThat(d.getSeUrl()).isEqualTo(SLUG);
    }

    @Test
    void metaTitleWinsOverTitleForTheSeoTitle() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentTranslation t = ContentFixtures.translation(ContentFixtures.EN, TITLE, BODY);
        t.setMetaTitle(SEO_TITLE);
        t.setFriendlyUrl("  about  ");

        ContentMapper.applyTranslations(entity, List.of(t), null, true);

        ContentDescription d = entity.getDescriptions().getFirst();
        assertThat(d.getTitle()).isEqualTo(SEO_TITLE);
        assertThat(d.getMetatagTitle()).isEqualTo(SEO_TITLE);
        assertThat(d.getSeUrl()).isEqualTo("about");
    }

    @Test
    void aBodylessTypeCountsATitleOnlyLocaleAsTranslated() {
        Content entity = ContentFixtures.content(1L, ContentType.BANNER, SLUG);

        ContentMapper.applyTranslations(entity,
                List.of(ContentFixtures.translation(ContentFixtures.EN, TITLE, null)), null, false);

        assertThat(entity.getDescriptions().getFirst().getState()).isEqualTo(TranslationState.TRANSLATED);
    }

    @Test
    void anIncompleteLocaleIsDraftAndAStaleOneStaysStale() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentTranslation incomplete = ContentFixtures.translation(ContentFixtures.EN, TITLE, null);
        ContentTranslation stale = ContentFixtures.translation(ContentFixtures.AR, ARABIC_TITLE, BODY);
        stale.setState(TranslationState.STALE);

        ContentMapper.applyTranslations(entity, List.of(incomplete, stale), null, true);

        assertThat(ContentMapper.locales(entity)).extracting("state")
                .containsExactly(TranslationState.STALE, TranslationState.DRAFT);
    }

    @Test
    void translationsComeBackSortedByLocale() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        entity.getDescriptions().add(ContentFixtures.description(entity, ContentFixtures.EN, TITLE, BODY));
        entity.getDescriptions().add(ContentFixtures.description(entity, ContentFixtures.AR, ARABIC_TITLE, BODY));

        assertThat(ContentMapper.translations(entity)).extracting(ContentTranslation::getLanguage)
                .containsExactly(ContentFixtures.AR, ContentFixtures.EN);
    }

    @Test
    void aLegacyRowCarriesTheSeoTitleInTheTitleColumn() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentDescription legacy = ContentFixtures.description(entity, ContentFixtures.EN, TITLE, BODY);
        legacy.setTitle(SEO_TITLE);

        assertThat(ContentMapper.translation(legacy).getMetaTitle()).isEqualTo(SEO_TITLE);
    }

    @Test
    void aTitleIdenticalToTheNameIsNotAMetaTitle() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentDescription d = ContentFixtures.description(entity, ContentFixtures.EN, TITLE, BODY);

        assertThat(ContentMapper.translation(d).getMetaTitle()).isNull();
    }

    @Test
    void populateCommonRoundTripsThroughApplyCommon() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        entity.setNoindex(true);
        entity.setOgMediaId(3L);
        entity.getDescriptions().add(ContentFixtures.description(entity, ContentFixtures.EN, TITLE, BODY));
        PersistablePage dto = new PersistablePage();

        ContentMapper.populateCommon(entity, dto);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getSlug()).isEqualTo(SLUG);
        assertThat(dto.isNoindex()).isTrue();
        assertThat(dto.getOgMediaId()).isEqualTo(3L);
        assertThat(dto.getTranslations()).hasSize(1);
    }

    @Test
    void auditReadsTheEmbeddedSection() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        AuditSection section = new AuditSection();
        section.setDateCreated(Instant.EPOCH);
        section.setDateModified(ContentFixtures.NOW);
        entity.setAuditSection(section);
        entity.setCreatedBy(ACTOR);
        entity.setUpdatedBy(EDITOR);

        assertThat(ContentMapper.audit(entity).getCreatedAt()).isEqualTo(Instant.EPOCH);
        assertThat(ContentMapper.audit(entity).getUpdatedAt()).isEqualTo(ContentFixtures.NOW);
        assertThat(ContentMapper.audit(entity).getCreatedBy()).isEqualTo(ACTOR);
        assertThat(ContentMapper.audit(entity).getUpdatedBy()).isEqualTo(EDITOR);
    }

    @Test
    void auditSurvivesARowWithoutAnAuditSection() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        entity.setAuditSection(null);

        assertThat(ContentMapper.audit(entity).getCreatedAt()).isNull();
    }

    @Test
    void titleFallsBackToTheFirstLocaleThenTheSlug() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        assertThat(ContentMapper.title(entity, ContentFixtures.EN)).isEqualTo(SLUG);

        entity.getDescriptions().add(ContentFixtures.description(entity, ContentFixtures.AR, ARABIC_TITLE, BODY));
        assertThat(ContentMapper.title(entity, ContentFixtures.EN)).isEqualTo(ARABIC_TITLE);
        assertThat(ContentMapper.title(entity, ContentFixtures.AR)).isEqualTo(ARABIC_TITLE);
    }

    @Test
    void aRowWithoutASubtitleShowsItsPath() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        entity.getDescriptions().add(ContentFixtures.description(entity, ContentFixtures.EN, TITLE, BODY));

        ReadableContentRow row = ContentMapper.row(entity, ContentFixtures.EN, null);

        assertThat(row.getSubtitle()).isEqualTo("/about-us");
        assertThat(row.getTitle()).isEqualTo(TITLE);
        assertThat(row.getType()).isEqualTo(ContentType.PAGE);
    }

    @Test
    void aGivenSubtitleWinsOverThePath() {
        Content entity = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        AuditSection section = new AuditSection();
        section.setDateModified(ContentFixtures.NOW);
        entity.setAuditSection(section);
        entity.setUpdatedBy(EDITOR);

        ReadableContentRow row = ContentMapper.row(entity, ContentFixtures.EN, GROUPED_SUBTITLE);

        assertThat(row.getSubtitle()).isEqualTo(GROUPED_SUBTITLE);
        assertThat(row.getUpdatedAt()).isEqualTo(ContentFixtures.NOW);
        assertThat(row.getUpdatedBy()).isEqualTo(EDITOR);
    }

}
