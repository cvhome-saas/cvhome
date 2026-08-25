package com.asrevo.cvhome.content.service.binding;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.PolicyType;
import com.asrevo.cvhome.content.model.PolicyVersionStatus;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.policy.PersistablePolicy;
import com.asrevo.cvhome.content.model.policy.PolicyMeta;
import com.asrevo.cvhome.content.model.policy.ReadablePolicy;
import com.asrevo.cvhome.content.service.PolicyService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Policy heads. "Publish policy" and "cut a new version" are the same gesture, so an {@code afterSave} on a
 * published head cuts a version whenever its text differs from the live one — and does nothing when it does not.
 */
class PolicyBindingTest {

    private static final String TERMS_SLUG = "terms";

    private static final String EU = "EU";

    private static final String UK = "UK";

    private static final String ACTOR = "ada";

    private static final String SLUG = TERMS_SLUG;

    private static final String HEADING = "Terms of service";

    private static final String BODY = "<p>body</p>";

    private PolicyService policies;

    private PolicyBinding binding;

    @BeforeEach
    void setUp() {
        policies = mock(PolicyService.class);
        binding = new PolicyBinding(policies);
    }

    private static Content head(ContentStatus status) {
        Content c = ContentFixtures.published(1L, ContentType.POLICY, SLUG, HEADING);
        c.setPolicyType(PolicyType.TERMS);
        c.setStatus(status);
        return c;
    }

    private static PolicyVersion live(String title, String body) {
        PolicyVersion v = new PolicyVersion();
        v.setVersion(1);
        v.setStatus(PolicyVersionStatus.LIVE);
        ContentTranslation t = ContentFixtures.translation(ContentFixtures.EN, title, body);
        v.setTranslations(JsonCodec.write(List.of(t)));
        return v;
    }

    @Test
    void theTypeContractIsThePolicyOne() {
        assertThat(binding.type()).isEqualTo(ContentType.POLICY);
        assertThat(binding.persistableClass()).isEqualTo(PersistablePolicy.class);
        assertThat(binding.newReadable()).isInstanceOf(ReadablePolicy.class);
        assertThat(binding.storefrontPath(head(ContentStatus.DRAFT))).isEqualTo("/policies/terms");
    }

    @Test
    void applyRefusesASecondHeadOfTheSameTypeThroughTheService() throws Exception {
        Content c = ContentFixtures.content(1L, ContentType.POLICY, SLUG);
        PersistablePolicy dto = new PersistablePolicy();
        dto.setPolicyType(PolicyType.TERMS);
        dto.setEffectiveFrom(ContentFixtures.NOW);
        dto.setJurisdiction(EU);
        dto.setRequiresAcceptance(true);
        dto.setShowAtCheckout(true);

        binding.apply(c, dto);

        verify(policies).assertTypeFree(ContentFixtures.STORE, PolicyType.TERMS, 1L);
        PolicyMeta meta = PolicyBinding.meta(c);
        assertThat(meta.jurisdiction()).isEqualTo(EU);
        assertThat(meta.requiresAcceptance()).isTrue();
        assertThat(meta.displayAt().checkout()).isTrue();
        assertThat(c.getStartsAt()).isEqualTo(ContentFixtures.NOW);
    }

    @Test
    void aRowWithoutMetaDefaultsToFooterOnly() {
        PolicyMeta meta = PolicyBinding.meta(ContentFixtures.content(1L, ContentType.POLICY, SLUG));

        assertThat(meta.displayAt().footer()).isTrue();
        assertThat(meta.displayAt().checkout()).isFalse();
        assertThat(meta.requiresAcceptance()).isFalse();
    }

    @Test
    void populateCarriesTheLiveVersionAndTheDisplayFlags() {
        Content c = head(ContentStatus.PUBLISHED);
        c.setMeta(JsonCodec.write(new PolicyMeta(UK, true, true,
                new PolicyMeta.DisplayAt(false, true, true))));
        when(policies.liveVersion(c)).thenReturn(3);
        when(policies.versions(c)).thenReturn(List.of());
        ReadablePolicy dto = new ReadablePolicy();

        binding.populate(c, dto);

        assertThat(dto.getLiveVersion()).isEqualTo(3);
        assertThat(dto.isShowInFooter()).isFalse();
        assertThat(dto.isShowAtCheckout()).isTrue();
        assertThat(dto.isShowAtSignup()).isTrue();
        assertThat(dto.isNotifyCustomers()).isTrue();
        assertThat(dto.getJurisdiction()).isEqualTo(UK);
    }

    @Test
    void aPolicyWithNoDisplayBlockShowsInTheFooter() {
        Content c = head(ContentStatus.DRAFT);
        c.setMeta(JsonCodec.write(new PolicyMeta(null, false, false, null)));
        when(policies.versions(c)).thenReturn(List.of());
        ReadablePolicy dto = new ReadablePolicy();

        binding.populate(c, dto);

        assertThat(dto.isShowInFooter()).isTrue();
        assertThat(dto.isShowAtCheckout()).isFalse();
    }

    @Test
    void anUnpublishedPolicyShowsItsTypeAloneInTheRowSubtitle() {
        Content c = head(ContentStatus.DRAFT);
        when(policies.liveVersion(c)).thenReturn(0);

        assertThat(binding.subtitle(c, ContentFixtures.EN)).isEqualTo(TERMS_SLUG);
    }

    @Test
    void aLivePolicyShowsItsVersionNumber() {
        Content c = head(ContentStatus.PUBLISHED);
        when(policies.liveVersion(c)).thenReturn(2);

        assertThat(binding.subtitle(c, ContentFixtures.EN)).isEqualTo("terms · v2");
    }

    @Test
    void aPolicyWithoutATypeStillHasASubtitle() {
        Content c = head(ContentStatus.DRAFT);
        c.setPolicyType(null);
        when(policies.liveVersion(c)).thenReturn(0);

        assertThat(binding.subtitle(c, ContentFixtures.EN)).isEqualTo("policy");
    }

    @Test
    void savingADraftCutsNoVersion() {
        binding.afterSave(head(ContentStatus.DRAFT));

        verify(policies, never()).publishVersion(any(), any(), any());
    }

    @Test
    void publishingWithNothingLiveCutsTheFirstVersion() {
        Content c = head(ContentStatus.PUBLISHED);
        c.setUpdatedBy(ACTOR);
        when(policies.live(c)).thenReturn(Optional.empty());

        binding.afterSave(c);

        verify(policies).publishVersion(org.mockito.ArgumentMatchers.eq(c), any(),
                org.mockito.ArgumentMatchers.eq(ACTOR));
    }

    @Test
    void republishingIdenticalTextCutsNothing() {
        Content c = head(ContentStatus.PUBLISHED);
        when(policies.live(c)).thenReturn(Optional.of(live(HEADING, BODY)));

        binding.afterSave(c);

        verify(policies, never()).publishVersion(any(), any(), any());
    }

    @Test
    void changedTextCutsANewVersion() {
        Content c = head(ContentStatus.PUBLISHED);
        when(policies.live(c)).thenReturn(Optional.of(live(HEADING, "<p>older</p>")));

        binding.afterSave(c);

        verify(policies).publishVersion(org.mockito.ArgumentMatchers.eq(c), any(), any());
    }

    @Test
    void aDifferentNumberOfLocalesCountsAsChangedText() {
        assertThat(PolicyBinding.sameText(List.of(), List.of(
                ContentFixtures.translation(ContentFixtures.EN, HEADING, BODY)))).isFalse();
    }

    @Test
    void deletingAPolicyForgetsEveryVersion() {
        Content c = head(ContentStatus.DRAFT);

        binding.afterDelete(c);

        verify(policies).forget(c);
    }

}
