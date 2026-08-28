package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.SocialLink;
import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.MediaAsset;
import com.asrevo.cvhome.content.entity.SiteSettings;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.site.PersistableSiteSettings;
import com.asrevo.cvhome.content.model.site.ReadableSiteSettings;
import com.asrevo.cvhome.content.model.site.SiteBranding;
import com.asrevo.cvhome.content.repository.MediaAssetRepository;
import com.asrevo.cvhome.content.repository.SiteSettingsRepository;
import com.asrevo.cvhome.content.storage.MediaStorage;
import com.asrevo.cvhome.content.support.JsonCodec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The store's appearance record — brand imagery, social links, site-level SEO.
 *
 * <p>
 * These used to be the merchant service's, spread over an upload endpoint per slot. Two things are different
 * here and both are tested below: a slot can be <em>cleared</em>, which merchant had no endpoint for at all, and
 * an asset has to belong to the store that names it, because the id now arrives from a client rather than from
 * an upload the server just performed.
 * </p>
 */
class SiteSettingsServiceTest {

    private static final String STORE_ID = ContentFixtures.STORE.getId();

    private static final String ACTOR = "org1-admin";

    private static final String LOGO_KEY = "files/store-1/media/7/logo.png";

    private static final String LOGO_URL = "https://cdn.test/logo.png";

    private static final String META_TITLE = "metaTitle";

    private static final String EN_TITLE = "Acme Supply Co.";

    private static final String AR_TITLE = "أكمي";

    private static final String EN = "en";

    private static final String AR = "ar";

    private static final String LOGO_ALT = "Acme logo";

    private static final String LOGO_ALT_AR = "شعار";

    private static final String FACEBOOK = "FACEBOOK";

    private SiteSettingsRepository repository;

    private MediaAssetRepository assets;

    private MediaUsageTracker usageTracker;

    private MediaStorage storage;

    private SiteSettingsService service;

    @BeforeEach
    void setUp() {
        repository = mock(SiteSettingsRepository.class);
        assets = mock(MediaAssetRepository.class);
        usageTracker = mock(MediaUsageTracker.class);
        storage = mock(MediaStorage.class);
        when(storage.url(LOGO_KEY)).thenReturn(LOGO_URL);
        service = new SiteSettingsService(repository, assets, usageTracker, storage, ContentFixtures.clock());
        when(repository.save(any(SiteSettings.class))).thenAnswer(i -> i.getArgument(0));
    }

    private static SiteSettings settings() {
        SiteSettings s = new SiteSettings();
        s.setStoreMerchantId(STORE_ID);
        return s;
    }

    private void stored(SiteSettings s) {
        when(repository.findById(STORE_ID)).thenReturn(Optional.of(s));
    }

    private static MediaAsset asset(long id, String storageKey, String altTexts) {
        MediaAsset a = new MediaAsset();
        a.setId(id);
        a.setStoreMerchantId(STORE_ID);
        a.setStorageKey(storageKey);
        a.setWidth(320);
        a.setHeight(120);
        a.setAltTexts(altTexts);
        return a;
    }

    private void library(MediaAsset... owned) {
        when(assets.findByStoreMerchantIdAndIdIn(anyString(), anyList())).thenAnswer(i -> {
            List<Long> wanted = i.getArgument(1);
            return List.of(owned).stream().filter(a -> wanted.contains(a.getId())).toList();
        });
    }

    private static PersistableSiteSettings body(Long logoId) {
        PersistableSiteSettings b = new PersistableSiteSettings();
        b.setLogoMediaId(logoId);
        return b;
    }

    /**
     * The row is made on first read rather than at provisioning, the same way the media quota row is — so a store
     * that predates this feature needs no backfill and no null check at every call site.
     */
    @Test
    void theRecordIsCreatedOnFirstReadRatherThanRequiringABackfill() {
        when(repository.findById(STORE_ID)).thenReturn(Optional.empty());

        SiteSettings created = service.entity(ContentFixtures.STORE);

        assertThat(created.getStoreMerchantId()).isEqualTo(STORE_ID);
        verify(repository).save(created);
    }

    @Test
    void anUntouchedRecordReadsAsEmptyRatherThanNull() {
        stored(settings());

        ReadableSiteSettings out = service.get(ContentFixtures.STORE, ContentFixtures.EN);

        assertThat(out.getLogoMediaId()).isNull();
        assertThat(out.getSeo()).isEmpty();
        assertThat(out.getSocialLinks()).isEmpty();
        assertThat(out.getBranding().logo()).isNull();
    }

    @Test
    void savingRecordsWhoChangedItAndWhen() throws ContentNotFoundException {
        SiteSettings entity = settings();
        stored(entity);
        library();

        service.put(ContentFixtures.STORE, body(null), ContentFixtures.EN, ACTOR);

        assertThat(entity.getUpdatedBy()).isEqualTo(ACTOR);
        assertThat(entity.getUpdatedAt()).isEqualTo(ContentFixtures.NOW);
    }

    /**
     * The whole record is replaced, so a {@code null} slot genuinely clears it. Merchant only ever had upload
     * endpoints, which is why a logo set once could not be removed.
     */
    @Test
    void aNullSlotClearsIt() throws ContentNotFoundException {
        SiteSettings entity = settings();
        entity.setLogoMediaId(7L);
        stored(entity);
        library(asset(7L, LOGO_KEY, null));

        ReadableSiteSettings out = service.put(ContentFixtures.STORE, body(null), ContentFixtures.EN, ACTOR);

        assertThat(entity.getLogoMediaId()).isNull();
        assertThat(out.getBranding().logo()).isNull();
    }

    /**
     * The id arrives from a client now rather than from an upload the server just did, so ownership is the
     * server's to check. Without this a store could point its logo at another tenant's asset and serve it.
     */
    @Test
    void anAssetFromAnotherStoreIsRefusedAndNothingIsWritten() {
        stored(settings());
        library(); // this store owns nothing

        assertThatThrownBy(() -> service.put(ContentFixtures.STORE, body(99L), ContentFixtures.EN, ACTOR))
                .isInstanceOf(ContentNotFoundException.class);

        verify(repository, never()).save(any(SiteSettings.class));
        verify(usageTracker, never()).replace(any(), any(), any(), any(), any(), any(), any());
    }

    /**
     * What the media library counts as a use, so an asset a slot holds cannot be deleted from under it. The whole
     * set is restated on every save, which is what makes a cleared slot release its asset.
     */
    @Test
    void theFilledSlotsAreStatedAsUsesAndTheEmptyOnesAreNot() throws ContentNotFoundException {
        SiteSettings entity = settings();
        stored(entity);
        library(asset(7L, LOGO_KEY, null));

        service.put(ContentFixtures.STORE, body(7L), ContentFixtures.EN, ACTOR);

        verify(usageTracker).replace(eq(ContentFixtures.STORE), eq(MediaOwnerKind.SITE_SETTINGS), eq(STORE_ID),
                eq(null), eq(null), anyString(), eq(Map.of("branding.logo", 7L)));
    }

    @Test
    void brandingResolvesTheSlotToAUrlAndItsDimensions() {
        SiteSettings entity = settings();
        entity.setLogoMediaId(7L);
        stored(entity);
        library(asset(7L, LOGO_KEY, null));

        SiteBranding branding = service.branding(ContentFixtures.STORE, ContentFixtures.EN);

        assertThat(branding.logo().url()).isEqualTo(LOGO_URL);
        assertThat(branding.logo().width()).isEqualTo(320);
        assertThat(branding.logo().height()).isEqualTo(120);
    }

    @Test
    void aStoreWithNoBrandImagesAsksTheLibraryForNothing() {
        stored(settings());

        assertThat(service.branding(ContentFixtures.STORE, ContentFixtures.EN).logo()).isNull();
        verify(assets, never()).findByStoreMerchantIdAndIdIn(anyString(), anyList());
    }

    @Test
    void altTextComesFromTheAssetInTheRequestedLocale() {
        SiteSettings entity = settings();
        entity.setLogoMediaId(7L);
        stored(entity);
        library(asset(7L, LOGO_KEY, JsonCodec.write(Map.of(EN, LOGO_ALT, AR, LOGO_ALT_AR))));

        assertThat(service.branding(ContentFixtures.STORE, ContentFixtures.AR).logo().alt()).isEqualTo(LOGO_ALT_AR);
    }

    /**
     * A store that wrote alt text in one language only still gets it in the other. An empty `alt` is worse than
     * the wrong language: it tells a screen reader the image carries nothing.
     */
    @Test
    void altTextFallsBackToWhateverLanguageWasWritten() {
        SiteSettings entity = settings();
        entity.setLogoMediaId(7L);
        stored(entity);
        library(asset(7L, LOGO_KEY, JsonCodec.write(Map.of(EN, LOGO_ALT))));

        assertThat(service.branding(ContentFixtures.STORE, ContentFixtures.AR).logo().alt()).isEqualTo(LOGO_ALT);
    }

    @Test
    void seoIsReadInTheRequestedLocale() {
        SiteSettings entity = settings();
        entity.setSeo(JsonCodec.write(Map.of(META_TITLE, Map.of(EN, EN_TITLE, AR, AR_TITLE))));

        assertThat(service.seoValue(entity, META_TITLE, ContentFixtures.EN)).isEqualTo(EN_TITLE);
        assertThat(service.seoValue(entity, META_TITLE, ContentFixtures.AR)).isEqualTo(AR_TITLE);
    }

    /**
     * An untranslated title falls back rather than leaving the tab blank — and a locale present but empty counts
     * as untranslated, because a stored empty string is what a half-filled console form leaves behind.
     */
    @Test
    void anUntranslatedSeoFieldFallsBackToWhateverWasWritten() {
        SiteSettings entity = settings();
        entity.setSeo(JsonCodec.write(Map.of(META_TITLE, Map.of(EN, EN_TITLE, AR, "  "))));

        assertThat(service.seoValue(entity, META_TITLE, ContentFixtures.AR)).isEqualTo(EN_TITLE);
    }

    @Test
    void anAbsentSeoFieldIsNullRatherThanAnError() {
        SiteSettings entity = settings();

        assertThat(service.seo(entity)).isEmpty();
        assertThat(service.seoValue(entity, META_TITLE, ContentFixtures.EN)).isNull();
        entity.setSeo(JsonCodec.write(Map.of(META_TITLE, Map.of())));
        assertThat(service.seoValue(entity, META_TITLE, ContentFixtures.EN)).isNull();
    }

    /**
     * Read back as an array rather than a {@code List}. A generic list deserialises to maps, and the cast to
     * {@code List<SocialLink>} then holds right up until the response is serialised — so the failure surfaced as
     * a 500 on read, nowhere near the code that caused it.
     */
    @Test
    void socialLinksComeBackAsLinksRatherThanMaps() throws ContentNotFoundException {
        SiteSettings entity = settings();
        stored(entity);
        library();
        PersistableSiteSettings b = body(null);
        b.setSocialLinks(List.of(new SocialLink(FACEBOOK, "https://facebook.com/acme")));

        service.put(ContentFixtures.STORE, b, ContentFixtures.EN, ACTOR);

        List<SocialLink> read = service.socialLinks(entity);
        assertThat(read).singleElement().isInstanceOf(SocialLink.class);
        assertThat(read.get(0).provider()).isEqualTo(FACEBOOK);
    }

    @Test
    void aStoreWithNoSocialLinksReadsAsAnEmptyListRatherThanNull() {
        assertThat(service.socialLinks(settings())).isEmpty();
    }

}
