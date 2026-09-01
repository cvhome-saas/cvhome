package com.asrevo.cvhome.content.service.binding;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.BannerPlacement;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.banner.BannerArtwork;
import com.asrevo.cvhome.content.model.banner.BannerMeta;
import com.asrevo.cvhome.content.model.banner.BannerTarget;
import com.asrevo.cvhome.content.model.banner.BannerTheme;
import com.asrevo.cvhome.content.model.banner.PersistableBanner;
import com.asrevo.cvhome.content.model.banner.ReadableBanner;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Banners. The capacity rule is the interesting one: a placement holds a fixed number of live banners, counted
 * only against the ones whose windows overlap this banner's, and COLLECTION counts per target.
 */
class BannerBindingTest {

    private static final String SALE_PATH = "/sale";

    private static final String TEXT_COLOUR = "#fff";

    private static final String DESKTOP_URL = "https://cdn.test/5.png";

    private static final String PLACEMENT_FIELD = "placement";

    private static final String DESKTOP_FIELD = "artwork.desktop";

    private static final String BANNER_SLUG = "spring-sale";

    private static final String ALT = "A field of tulips";

    private ContentRepository repository;

    private MediaService media;

    private BannerBinding binding;

    @BeforeEach
    void setUp() {
        repository = mock(ContentRepository.class);
        media = mock(MediaService.class);
        binding = new BannerBinding(media, repository, ContentFixtures.clock());
    }

    private static Content banner(Long id, BannerPlacement placement, ContentStatus status) {
        Content c = ContentFixtures.published(id, ContentType.BANNER, String.format("b-%d", id), "Sale");
        c.setPlacement(placement);
        c.setStatus(status);
        c.setVisible(status == ContentStatus.PUBLISHED);
        return c;
    }

    @Test
    void theTypeContractIsTheBannerOne() {
        assertThat(binding.type()).isEqualTo(ContentType.BANNER);
        assertThat(binding.persistableClass()).isEqualTo(PersistableBanner.class);
        assertThat(binding.newReadable()).isInstanceOf(ReadableBanner.class);
        assertThat(binding.requiresBody()).isFalse();
        assertThat(binding.storefrontPath(banner(1L, BannerPlacement.COLLECTION, ContentStatus.DRAFT))).isNull();
    }

    /**
     * The announcement strip is the one banner whose copy is a body: it renders no artwork and no headline, so its
     * message is the {@code description} — which is what the {@code header-message} box it replaced held. Every
     * other placement lays its copy over artwork and needs no body at all.
     */
    @Test
    void onlyTheAnnouncementStripNeedsABody() {
        assertThat(binding.requiresBody(banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT))).isTrue();
        assertThat(binding.requiresBody(banner(2L, BannerPlacement.COLLECTION, ContentStatus.DRAFT))).isFalse();
        assertThat(binding.requiresBody(banner(3L, BannerPlacement.COLLECTION, ContentStatus.DRAFT))).isFalse();
        assertThat(binding.requiresBody(ContentFixtures.content(4L, ContentType.BANNER, BANNER_SLUG))).isFalse();
    }

    @Test
    void applyWritesPlacementWindowAndMeta() {
        Content c = ContentFixtures.content(1L, ContentType.BANNER, BANNER_SLUG);
        PersistableBanner dto = new PersistableBanner();
        dto.setPlacement(BannerPlacement.COLLECTION);
        dto.setStartsAt(ContentFixtures.NOW);
        dto.setEndsAt(ContentFixtures.NOW.plusSeconds(60));
        dto.setTarget(new BannerTarget(BannerTarget.Kind.URL, SALE_PATH));
        dto.setArtwork(new BannerArtwork(5L, 6L, null));
        dto.setTheme(new BannerTheme(TEXT_COLOUR, 40, "left"));
        dto.setLoggedInOnly(true);

        binding.apply(c, dto);

        assertThat(c.getPlacement()).isEqualTo(BannerPlacement.COLLECTION);
        assertThat(c.getEndsAt()).isEqualTo(ContentFixtures.NOW.plusSeconds(60));
        BannerMeta meta = BannerBinding.meta(c);
        assertThat(meta.loggedInOnly()).isTrue();
        assertThat(meta.artwork().desktopMediaId()).isEqualTo(5L);
        assertThat(meta.theme().textColor()).isEqualTo(TEXT_COLOUR);
    }

    @Test
    void aRowWithoutMetaReadsAsAnEmptyBannerMeta() {
        BannerMeta meta = BannerBinding.meta(ContentFixtures.content(1L, ContentType.BANNER, BANNER_SLUG));

        assertThat(meta.target()).isNull();
        assertThat(meta.artwork()).isNull();
        assertThat(meta.loggedInOnly()).isFalse();
    }

    @Test
    void populateResolvesTheArtworkUrls() {
        Content c = banner(1L, BannerPlacement.COLLECTION, ContentStatus.PUBLISHED);
        c.setMeta(JsonCodec.write(new BannerMeta(null, new BannerArtwork(5L, null, null), null, false)));
        // a HashMap, like the real MediaService returns: populate looks up the unset mobile slot by a null key
        Map<Long, String> urls = new java.util.HashMap<>();
        urls.put(5L, DESKTOP_URL);
        when(media.urls(any(), anyList())).thenReturn(urls);
        ReadableBanner dto = new ReadableBanner();

        binding.populate(c, dto);

        assertThat(dto.getDesktopUrl()).isEqualTo(DESKTOP_URL);
        assertThat(dto.getMobileUrl()).isNull();
        assertThat(dto.getPlacement()).isEqualTo(BannerPlacement.COLLECTION);
        assertThat(dto.getStatus()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(dto.getLocales()).hasSize(1);
    }

    @Test
    void aBannerWithoutArtworkAsksTheMediaServiceNothing() {
        Content c = banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        ReadableBanner dto = new ReadableBanner();

        binding.populate(c, dto);

        assertThat(dto.getDesktopUrl()).isNull();
        org.mockito.Mockito.verify(media, org.mockito.Mockito.never()).urls(any(), anyList());
    }

    @Test
    void theRowSubtitleIsThePlacementAndTheTargetWhenThereIsOne() {
        Content plain = banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        Content targeted = banner(2L, BannerPlacement.COLLECTION, ContentStatus.DRAFT);
        targeted.setMeta(JsonCodec.write(new BannerMeta(new BannerTarget(BannerTarget.Kind.URL, SALE_PATH), null,
                null, false)));
        Content unplaced = banner(3L, null, ContentStatus.DRAFT);

        assertThat(binding.subtitle(plain, ContentFixtures.EN)).isEqualTo("strip");
        assertThat(binding.subtitle(targeted, ContentFixtures.EN)).isEqualTo("collection · /sale");
        assertThat(binding.subtitle(unplaced, ContentFixtures.EN)).isEqualTo("—");
    }

    @Test
    void artworkWithoutAltTextBlocksPublishOutsideTheStrip() {
        Content c = banner(1L, BannerPlacement.COLLECTION, ContentStatus.DRAFT);
        c.setMeta(JsonCodec.write(new BannerMeta(null, new BannerArtwork(5L, null, null), null, false)));
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of());
        ContentDescription source = c.getDescriptions().getFirst();

        assertThat(binding.publishProblems(c, source)).extracting(FieldError::field)
                .containsExactly("translations.en.altText");
    }

    @Test
    void aStripBannerNeedsNoAltText() {
        Content c = banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        c.setMeta(JsonCodec.write(new BannerMeta(null, new BannerArtwork(5L, null, null), null, false)));
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER)).thenReturn(List.of());

        assertThat(binding.publishProblems(c, c.getDescriptions().getFirst())).isEmpty();
    }

    @Test
    void aBannerWithoutAPlacementCannotBePublishedAndIsNotCapacityChecked() {
        Content c = banner(1L, null, ContentStatus.DRAFT);

        assertThat(binding.publishProblems(c, c.getDescriptions().getFirst()))
                .extracting(FieldError::field).containsExactly(PLACEMENT_FIELD);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).findAllByType(any(), any());
    }

    @Test
    void aFullPlacementWithAnOverlappingWindowIsRefused() {
        Content candidate = banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        candidate.getDescriptions().getFirst().setAltText(ALT);
        Content live = banner(2L, BannerPlacement.STRIP, ContentStatus.PUBLISHED);
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER))
                .thenReturn(List.of(candidate, live));

        assertThat(binding.publishProblems(candidate, candidate.getDescriptions().getFirst()))
                .singleElement().satisfies(f -> {
                    assertThat(f.field()).isEqualTo(PLACEMENT_FIELD);
                    assertThat(f.message()).contains("is full");
                });
    }

    @Test
    void aScheduledBannerCountsTowardsCapacityButADraftDoesNot() {
        Content candidate = banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        Content scheduled = banner(2L, BannerPlacement.STRIP, ContentStatus.SCHEDULED);
        Content draft = banner(3L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER))
                .thenReturn(List.of(candidate, draft));
        assertThat(binding.capacityConflict(candidate)).isNull();

        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER))
                .thenReturn(List.of(candidate, scheduled));
        assertThat(binding.capacityConflict(candidate)).isEqualTo(2L);
    }

    @Test
    void nonOverlappingWindowsShareAPlacement() {
        Content candidate = banner(1L, BannerPlacement.STRIP, ContentStatus.DRAFT);
        candidate.setStartsAt(ContentFixtures.NOW);
        candidate.setEndsAt(ContentFixtures.NOW.plusSeconds(60));
        Content earlier = banner(2L, BannerPlacement.STRIP, ContentStatus.PUBLISHED);
        earlier.setStartsAt(ContentFixtures.NOW.minusSeconds(120));
        earlier.setEndsAt(ContentFixtures.NOW.minusSeconds(60));
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER))
                .thenReturn(List.of(candidate, earlier));

        assertThat(binding.capacityConflict(candidate)).isNull();
    }

    @Test
    void aCollectionPlacementCountsOnlyBannersAimedAtTheSameTarget() {
        BannerTarget shoes = new BannerTarget(BannerTarget.Kind.COLLECTION, "shoes");
        Content candidate = banner(1L, BannerPlacement.COLLECTION, ContentStatus.DRAFT);
        candidate.setMeta(JsonCodec.write(new BannerMeta(shoes, null, null, false)));
        Content otherTarget = banner(2L, BannerPlacement.COLLECTION, ContentStatus.PUBLISHED);
        otherTarget.setMeta(JsonCodec.write(new BannerMeta(
                new BannerTarget(BannerTarget.Kind.COLLECTION, "bags"), null, null, false)));
        Content sameTarget = banner(3L, BannerPlacement.COLLECTION, ContentStatus.PUBLISHED);
        sameTarget.setMeta(JsonCodec.write(new BannerMeta(shoes, null, null, false)));
        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER))
                .thenReturn(List.of(candidate, otherTarget));
        assertThat(binding.capacityConflict(candidate)).isNull();

        when(repository.findAllByType(ContentFixtures.STORE, ContentType.BANNER))
                .thenReturn(List.of(candidate, sameTarget));
        assertThat(binding.capacityConflict(candidate)).isEqualTo(3L);
    }

    @Test
    void mediaReferencesNameEachArtworkSlotThatIsSet() {
        Content c = banner(1L, BannerPlacement.COLLECTION, ContentStatus.DRAFT);
        assertThat(binding.mediaReferences(c)).isEmpty();

        c.setMeta(JsonCodec.write(new BannerMeta(null, new BannerArtwork(5L, null, null), null, false)));
        assertThat(binding.mediaReferences(c)).containsExactly(Map.entry(DESKTOP_FIELD, 5L));

        c.setMeta(JsonCodec.write(new BannerMeta(null, new BannerArtwork(5L, 6L, null), null, false)));
        assertThat(binding.mediaReferences(c)).containsOnlyKeys(DESKTOP_FIELD, "artwork.mobile");
    }

    @Test
    void aBannerIsEffectiveOnlyInsideBothItsWindows() {
        Content c = banner(1L, BannerPlacement.COLLECTION, ContentStatus.PUBLISHED);
        assertThat(binding.effective(c)).isTrue();

        c.setStartsAt(ContentFixtures.NOW.plusSeconds(60));
        assertThat(binding.effective(c)).isFalse();

        c.setStartsAt(ContentFixtures.NOW.minusSeconds(60));
        c.setEndsAt(ContentFixtures.NOW.minusSeconds(1));
        assertThat(binding.effective(c)).isFalse();

        c.setEndsAt(Instant.MAX);
        assertThat(binding.effective(c)).isTrue();
    }

    @Test
    void anUnpublishedBannerIsNeverEffective() {
        assertThat(binding.effective(banner(1L, BannerPlacement.COLLECTION, ContentStatus.DRAFT))).isFalse();
    }

    @Test
    void afterSaveDoesNothingOnADraft() {
        binding.afterSave(banner(1L, BannerPlacement.COLLECTION, ContentStatus.DRAFT));

        org.mockito.Mockito.verifyNoInteractions(media);
    }

}
