package com.asrevo.cvhome.content.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.entity.ContentStatusAudit;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.PublishRequest;
import com.asrevo.cvhome.content.model.page.PersistablePage;
import com.asrevo.cvhome.content.model.page.ReadablePage;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.ContentStatusAuditRepository;
import com.asrevo.cvhome.content.service.binding.PageBinding;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The status state machine. The rules under test: a future {@code publishAt} turns a publish into a schedule, a
 * publish is refused while the source locale is incomplete, {@code visible} tracks the status, and the scheduler
 * sweep is idempotent because its predicates exclude anything already moved.
 */
class PublishingServiceTest {

    private static final String BODY = "<p>body</p>";

    private static final String ARABIC_TITLE = "عن";

    private static final String ACTOR = "ada";

    private static final String SLUG = "about-us";

    private static final String TITLE = "About us";

    private ContentRepository contents;

    private ContentStatusAuditRepository audits;

    private PublishingService service;

    private ContentTypeBinding<PersistablePage, ReadablePage> binding;

    @BeforeEach
    void setUp() {
        contents = mock(ContentRepository.class);
        audits = mock(ContentStatusAuditRepository.class);
        binding = new PageBinding();
        service = new PublishingService(contents, audits, ContentFixtures.clock());
    }

    private Content complete() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.EN, TITLE, BODY));
        return c;
    }

    @Test
    void publishingMakesTheRowVisibleAndWritesTheAuditRow() throws Exception {
        Content c = complete();

        service.transition(c, ContentStatus.PUBLISHED, null, ContentFixtures.EN, binding, ACTOR, null);

        assertThat(c.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(c.isVisible()).isTrue();
        assertThat(c.getPublishAt()).isEqualTo(ContentFixtures.NOW);
        var captor = forClass(ContentStatusAudit.class);
        verify(audits).save(captor.capture());
        assertThat(captor.getValue().getFromStatus()).isEqualTo(ContentStatus.DRAFT);
        assertThat(captor.getValue().getToStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(captor.getValue().getActor()).isEqualTo(ACTOR);
    }

    @Test
    void aFuturePublishAtBecomesAScheduleRatherThanAPublish() throws Exception {
        Content c = complete();
        PublishRequest request = new PublishRequest();
        request.setPublishAt(ContentFixtures.NOW.plusSeconds(3600));

        service.transition(c, ContentStatus.PUBLISHED, request, ContentFixtures.EN, binding, ACTOR, null);

        assertThat(c.getStatus()).isEqualTo(ContentStatus.SCHEDULED);
        assertThat(c.isVisible()).isFalse();
        assertThat(c.getPublishAt()).isEqualTo(ContentFixtures.NOW.plusSeconds(3600));
    }

    @Test
    void aPublishAtInsideTheGracePeriodPublishesNow() throws Exception {
        Content c = complete();
        PublishRequest request = new PublishRequest();
        request.setPublishAt(ContentFixtures.NOW.plusSeconds(5));

        service.transition(c, ContentStatus.PUBLISHED, request, ContentFixtures.EN, binding, ACTOR, null);

        assertThat(c.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
    }

    @Test
    void schedulingWithoutAFutureDateIsRejected() {
        Content c = complete();

        assertThatThrownBy(() -> service.transition(c, ContentStatus.SCHEDULED, null, ContentFixtures.EN, binding,
                ACTOR, null))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("publishAt must be in the future");
    }

    @Test
    void anUnpublishDateMustFollowThePublishDate() {
        Content c = complete();
        PublishRequest request = new PublishRequest();
        request.setPublishAt(ContentFixtures.NOW.plusSeconds(3600));
        request.setUnpublishAt(ContentFixtures.NOW.plusSeconds(60));

        assertThatThrownBy(() -> service.transition(c, ContentStatus.PUBLISHED, request, ContentFixtures.EN,
                binding, ACTOR, null))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("unpublishAt must be after publishAt");
    }

    @Test
    void anUnpublishDateInThePastIsRejected() {
        Content c = complete();
        PublishRequest request = new PublishRequest();
        request.setUnpublishAt(ContentFixtures.NOW.minusSeconds(60));

        assertThatThrownBy(() -> service.transition(c, ContentStatus.PUBLISHED, request, ContentFixtures.EN,
                binding, ACTOR, null))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("unpublishAt must be in the future");
    }

    @Test
    void anUnpublishWindowIsStoredOnTheRow() throws Exception {
        Content c = complete();
        PublishRequest request = new PublishRequest();
        request.setUnpublishAt(ContentFixtures.NOW.plusSeconds(3600));

        service.transition(c, ContentStatus.PUBLISHED, request, ContentFixtures.EN, binding, ACTOR, null);

        assertThat(c.getUnpublishAt()).isEqualTo(ContentFixtures.NOW.plusSeconds(3600));
    }

    @Test
    void movingToTheStatusItAlreadyHasChangesNothing() throws Exception {
        Content c = complete();
        c.setStatus(ContentStatus.DRAFT);

        service.transition(c, ContentStatus.DRAFT, null, ContentFixtures.EN, binding, ACTOR, null);

        verify(audits, never()).save(any());
    }

    @Test
    void anIllegalTransitionIsRefused() {
        Content c = complete();
        c.setStatus(ContentStatus.ARCHIVED);

        assertThatThrownBy(() -> service.transition(c, ContentStatus.REVIEW, null, ContentFixtures.EN, binding,
                ACTOR, null))
                .isInstanceOf(ContentRuleException.class);
    }

    @Test
    void unpublishingAndArchivingClearThePublishDate() throws Exception {
        Content c = complete();
        c.setStatus(ContentStatus.PUBLISHED);
        c.setPublishAt(ContentFixtures.NOW);

        service.transition(c, ContentStatus.DRAFT, null, ContentFixtures.EN, binding, ACTOR, null);

        assertThat(c.getPublishAt()).isNull();
        assertThat(c.isVisible()).isFalse();
    }

    @Test
    void publishingWithNoLocaleAtAllNamesTheTranslationsField() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);

        assertThatThrownBy(() -> service.gate(c, ContentFixtures.EN, binding))
                .isInstanceOf(ContentRuleException.class)
                .satisfies(e -> assertThat(((ContentRuleException) e).payload().fieldErrors())
                        .extracting(FieldError::field).containsExactly("translations"));
    }

    @Test
    void publishingAPageWithoutABodyIsRefused() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.EN, TITLE, null));

        assertThatThrownBy(() -> service.gate(c, ContentFixtures.EN, binding))
                .isInstanceOf(ContentRuleException.class)
                .satisfies(e -> assertThat(((ContentRuleException) e).payload().fieldErrors())
                        .extracting(FieldError::field).containsExactly("translations.en.body"));
    }

    @Test
    void publishingWithoutATitleIsRefused() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentDescription d = ContentFixtures.description(c, ContentFixtures.EN, null, BODY);
        d.setTitle(null);
        c.getDescriptions().add(d);

        assertThatThrownBy(() -> service.gate(c, ContentFixtures.EN, binding))
                .isInstanceOf(ContentRuleException.class)
                .satisfies(e -> assertThat(((ContentRuleException) e).payload().fieldErrors())
                        .extracting(FieldError::field).containsExactly("translations.en.title"));
    }

    @Test
    void anAbsentSourceLocaleFallsBackToACompleteOne() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        c.getDescriptions().add(ContentFixtures.description(c, ContentFixtures.AR, ARABIC_TITLE, "<p>نص</p>"));

        assertThatCode(() -> service.gate(c, ContentFixtures.EN, binding)).doesNotThrowAnyException();
    }

    @Test
    void withNoTranslatedLocaleAtAllTheFirstRowIsGated() {
        Content c = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        ContentDescription d = ContentFixtures.description(c, ContentFixtures.AR, ARABIC_TITLE, null);
        d.setState(TranslationState.DRAFT);
        c.getDescriptions().add(d);

        assertThatThrownBy(() -> service.gate(c, ContentFixtures.EN, binding))
                .isInstanceOf(ContentRuleException.class)
                .satisfies(e -> assertThat(((ContentRuleException) e).payload().fieldErrors())
                        .extracting(FieldError::field).containsExactly("translations.ar.body"));
    }

    @Test
    void theSweepPublishesWhatIsDueAndArchivesWhatExpired() {
        Content due = ContentFixtures.content(1L, ContentType.PAGE, SLUG);
        due.setStatus(ContentStatus.SCHEDULED);
        Content expired = ContentFixtures.published(2L, ContentType.PAGE, "terms", TITLE);
        expired.setUnpublishAt(ContentFixtures.NOW.minusSeconds(60));
        when(contents.findDue(ContentStatus.SCHEDULED, ContentFixtures.NOW)).thenReturn(List.of(due));
        when(contents.findExpired(ContentStatus.PUBLISHED, ContentFixtures.NOW)).thenReturn(List.of(expired));

        assertThat(service.tick()).isEqualTo(2);

        assertThat(due.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(due.isVisible()).isTrue();
        assertThat(expired.getStatus()).isEqualTo(ContentStatus.ARCHIVED);
        assertThat(expired.isVisible()).isFalse();
        assertThat(expired.getUnpublishAt()).isNull();
    }

    @Test
    void aSweepWithNothingDueChangesNothing() {
        when(contents.findDue(ContentStatus.SCHEDULED, ContentFixtures.NOW)).thenReturn(List.of());
        when(contents.findExpired(ContentStatus.PUBLISHED, ContentFixtures.NOW)).thenReturn(List.of());

        assertThat(service.tick()).isZero();

        verify(contents, never()).save(any());
    }

}
