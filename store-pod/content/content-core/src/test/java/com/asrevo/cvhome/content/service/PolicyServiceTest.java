package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.PolicyVersionStatus;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.policy.PolicyCompliance;
import com.asrevo.cvhome.content.model.policy.PublishPolicyVersionRequest;
import com.asrevo.cvhome.content.model.policy.ReadablePolicyVersion;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.PolicyVersionRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Immutable policy versions. Publishing the head cuts version n+1 and archives the previous LIVE one; the archive
 * is flushed before the insert because the partial unique index allows one LIVE row per policy.
 */
class PolicyServiceTest {

    private static final String NOTE = "GDPR update";

    private static final String ORIGINAL_NOTE = "original";

    private static final String ACTOR = "ada";

    private static final String TERMS = "terms";

    private static final String HEADING = "Terms of service";

    private PolicyVersionRepository versions;

    private ContentRepository contents;

    private PolicyService service;

    @BeforeEach
    void setUp() {
        versions = mock(PolicyVersionRepository.class);
        contents = mock(ContentRepository.class);
        service = new PolicyService(versions, contents, ContentFixtures.clock());
    }

    private static Content head() {
        Content c = ContentFixtures.published(1L, ContentType.POLICY, TERMS, HEADING);
        c.setPolicyType(PolicyType.TERMS);
        return c;
    }

    private static PolicyVersion version(int number, PolicyVersionStatus status) {
        PolicyVersion v = new PolicyVersion();
        v.setId((long) number);
        v.setContentId(1L);
        v.setVersion(number);
        v.setStatus(status);
        v.setPublishedAt(ContentFixtures.NOW);
        v.setTranslations(JsonCodec.write(List.of(
                ContentFixtures.translation(ContentFixtures.EN, HEADING, "<p>body</p>"))));
        return v;
    }

    @Test
    void theVersionListCarriesNoTextButASingleVersionDoes() throws Exception {
        when(versions.findByContentIdOrderByVersionDesc(1L))
                .thenReturn(List.of(version(2, PolicyVersionStatus.LIVE), version(1, PolicyVersionStatus.ARCHIVED)));
        when(versions.findByContentIdAndVersion(1L, 2)).thenReturn(Optional.of(version(2,
                PolicyVersionStatus.LIVE)));

        List<ReadablePolicyVersion> list = service.versions(head());

        assertThat(list).extracting(ReadablePolicyVersion::getVersion).containsExactly(2, 1);
        assertThat(list.getFirst().getTranslations()).isNull();
        assertThat(service.version(head(), 2).getTranslations()).hasSize(1);
    }

    @Test
    void readingAVersionThatDoesNotExistReadsAsMissing() {
        when(versions.findByContentIdAndVersion(1L, 9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.version(head(), 9)).isInstanceOf(ContentNotFoundException.class);
        assertThatThrownBy(() -> service.textOf(head(), 9)).isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void theLiveVersionNumberIsZeroWhileNothingIsPublished() {
        when(versions.findFirstByContentIdAndStatus(1L, PolicyVersionStatus.LIVE)).thenReturn(Optional.empty());

        assertThat(service.liveVersion(head())).isZero();
        assertThat(PolicyService.isLive(head(), Optional.empty())).isFalse();
    }

    @Test
    void aPublishedHeadWithALiveVersionCountsAsLive() {
        assertThat(PolicyService.isLive(head(), Optional.of(version(1, PolicyVersionStatus.LIVE)))).isTrue();
        Content draft = head();
        draft.setStatus(ContentStatus.DRAFT);
        assertThat(PolicyService.isLive(draft, Optional.of(version(1, PolicyVersionStatus.LIVE)))).isFalse();
    }

    @Test
    void thePreviousLiveVersionIsArchivedBeforeTheNewOneIsInserted() {
        PolicyVersion previous = version(1, PolicyVersionStatus.LIVE);
        when(versions.findByContentIdOrderByVersionDesc(1L)).thenReturn(List.of(previous));
        when(versions.findFirstByContentIdAndStatus(1L, PolicyVersionStatus.LIVE)).thenReturn(Optional.of(previous));
        when(versions.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        ReadablePolicyVersion out = service.publishVersion(head(), null, ACTOR);

        assertThat(previous.getStatus()).isEqualTo(PolicyVersionStatus.ARCHIVED);
        assertThat(out.getVersion()).isEqualTo(2);
        assertThat(out.getStatus()).isEqualTo(PolicyVersionStatus.LIVE);
        assertThat(out.getEffectiveFrom()).isEqualTo(ContentFixtures.NOW);
        assertThat(out.getPublishedBy()).isEqualTo(ACTOR);
        assertThat(out.getTranslations()).hasSize(1);
    }

    @Test
    void theFirstCutIsVersionOneAndTakesTheRequestedDate() {
        when(versions.findByContentIdOrderByVersionDesc(1L)).thenReturn(List.of());
        when(versions.findFirstByContentIdAndStatus(1L, PolicyVersionStatus.LIVE)).thenReturn(Optional.empty());
        when(versions.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        PublishPolicyVersionRequest request = new PublishPolicyVersionRequest();
        request.setEffectiveFrom(ContentFixtures.NOW.plusSeconds(600));
        request.setNote(NOTE);

        ReadablePolicyVersion out = service.publishVersion(head(), request, ACTOR);

        assertThat(out.getVersion()).isEqualTo(1);
        assertThat(out.getEffectiveFrom()).isEqualTo(ContentFixtures.NOW.plusSeconds(600));
        assertThat(out.getNote()).isEqualTo(NOTE);
    }

    @Test
    void annotatingWithNothingLiveCutsAVersionInstead() {
        when(versions.findFirstByContentIdAndStatus(1L, PolicyVersionStatus.LIVE)).thenReturn(Optional.empty());
        when(versions.findByContentIdOrderByVersionDesc(1L)).thenReturn(List.of());
        when(versions.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(service.annotateLive(head(), null, ACTOR).getVersion()).isEqualTo(1);
    }

    @Test
    void annotatingSetsOnlyTheFieldsThatWereSent() {
        PolicyVersion live = version(3, PolicyVersionStatus.LIVE);
        live.setNote(ORIGINAL_NOTE);
        when(versions.findFirstByContentIdAndStatus(1L, PolicyVersionStatus.LIVE)).thenReturn(Optional.of(live));
        when(versions.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        PublishPolicyVersionRequest request = new PublishPolicyVersionRequest();
        request.setEffectiveFrom(ContentFixtures.NOW);

        assertThat(service.annotateLive(head(), request, ACTOR).getNote()).isEqualTo(ORIGINAL_NOTE);
        assertThat(live.getEffectiveFrom()).isEqualTo(ContentFixtures.NOW);
    }

    @Test
    void annotatingWithNoRequestAtAllLeavesTheLiveVersionAlone() {
        PolicyVersion live = version(3, PolicyVersionStatus.LIVE);
        when(versions.findFirstByContentIdAndStatus(1L, PolicyVersionStatus.LIVE)).thenReturn(Optional.of(live));
        when(versions.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        assertThat(service.annotateLive(head(), null, ACTOR).getVersion()).isEqualTo(3);
    }

    @Test
    void oldTextIsReadableWithoutTouchingTheVersion() throws Exception {
        PolicyVersion old = version(1, PolicyVersionStatus.ARCHIVED);
        when(versions.findByContentIdAndVersion(1L, 1)).thenReturn(Optional.of(old));

        List<ContentTranslation> text = service.textOf(head(), 1);

        assertThat(text).extracting(ContentTranslation::getTitle).containsExactly(HEADING);
        assertThat(old.getStatus()).isEqualTo(PolicyVersionStatus.ARCHIVED);
    }

    @Test
    void aVersionWithoutStoredTextReadsAsAnEmptyList() {
        PolicyVersion v = version(1, PolicyVersionStatus.LIVE);
        v.setTranslations(null);

        assertThat(PolicyService.translations(v)).isEmpty();
    }

    @Test
    void forgettingAPolicyDropsEveryVersion() {
        service.forget(head());

        verify(versions).deleteByContentId(1L);
    }

    @Test
    void aSecondHeadOfTheSameTypeIsRefused() {
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(head()));

        assertThatThrownBy(() -> service.assertTypeFree(ContentFixtures.STORE, PolicyType.TERMS, 2L))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void aHeadEditingItselfIsNotASecondHead() throws Exception {
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(head()));

        service.assertTypeFree(ContentFixtures.STORE, PolicyType.TERMS, 1L);
        service.assertTypeFree(ContentFixtures.STORE, PolicyType.PRIVACY, 2L);
    }

    @Test
    void complianceCoversEveryTypeExceptCustom() {
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.POLICY)).thenReturn(List.of(head()));

        List<PolicyCompliance> out = service.compliance(ContentFixtures.STORE);

        assertThat(out).noneMatch(c -> c.getType() == PolicyType.CUSTOM);
        assertThat(out).filteredOn(c -> c.getType() == PolicyType.TERMS).singleElement().satisfies(c -> {
            assertThat(c.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
            assertThat(c.getId()).isEqualTo(1L);
            assertThat(c.getRequiredBy()).contains("EU");
        });
        assertThat(out).filteredOn(c -> c.getType() == PolicyType.SHIPPING).singleElement()
                .satisfies(c -> assertThat(c.getStatus()).isNull());
    }

}
