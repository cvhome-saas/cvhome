package com.asrevo.cvhome.content.service.binding;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.model.HomeSectionKind;
import com.asrevo.cvhome.content.model.section.PersistableSection;
import com.asrevo.cvhome.content.model.section.ReadableSection;
import com.asrevo.cvhome.content.model.section.SectionMeta;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Home-page sections.
 *
 * <p>
 * The interesting rule is what a section has to carry before it can be published. A block that renders nothing is
 * worse than a missing one — it reaches the home page as a gap the seller cannot see the cause of — so the kinds
 * that collect something are refused without a target, and an image section without an image.
 * </p>
 */
class SectionBindingTest {

    private static final String FEATURED = "FEATURED_ITEMS";

    private static final String KIND_FIELD = "kind";

    private static final String TARGET_FIELD = "targetValue";

    private static final String MEDIA_FIELD = "mediaId";

    private static final String IMAGE_URL = "https://cdn.test/hero.png";

    private static final String CAROUSEL = "carousel";

    private MediaService media;

    private SectionBinding binding;

    @BeforeEach
    void setUp() {
        media = mock(MediaService.class);
        binding = new SectionBinding(media);
    }

    private static Content section(SectionMeta meta) {
        Content c = ContentFixtures.published(7L, ContentType.SECTION, "s-7", "Featured");
        c.setMeta(meta == null ? null : JsonCodec.write(meta));
        return c;
    }

    private static SectionMeta meta(HomeSectionKind kind, String target, Long mediaId) {
        return new SectionMeta(kind, target, mediaId, null, null, null);
    }

    @Test
    void theTypeContractIsTheSectionOne() {
        assertThat(binding.type()).isEqualTo(ContentType.SECTION);
        assertThat(binding.persistableClass()).isEqualTo(PersistableSection.class);
        assertThat(binding.newReadable()).isInstanceOf(ReadableSection.class);
    }

    /**
     * A section's copy is a heading and maybe a subtitle. Requiring a body would make a `PRODUCT_GROUP` rail —
     * which has no prose at all — impossible to publish.
     */
    @Test
    void aSectionPublishesWithoutABody() {
        assertThat(binding.requiresBody()).isFalse();
    }

    @Test
    void everythingTypeSpecificRoundTripsThroughMeta() {
        PersistableSection dto = new PersistableSection();
        dto.setKind(HomeSectionKind.PRODUCT_GROUP);
        dto.setTargetValue(FEATURED);
        dto.setItemLimit(8);
        dto.setLayout(CAROUSEL);
        Content entity = section(null);

        binding.apply(entity, dto);

        SectionMeta stored = SectionBinding.meta(entity);
        assertThat(stored.kind()).isEqualTo(HomeSectionKind.PRODUCT_GROUP);
        assertThat(stored.targetValue()).isEqualTo(FEATURED);
        assertThat(stored.itemLimit()).isEqualTo(8);
        assertThat(stored.layout()).isEqualTo(CAROUSEL);
    }

    /**
     * A row whose {@code meta} is absent or unreadable reads as an empty section rather than throwing. It is
     * still refused at publish — by the kind check below — which is the right place to catch it: the console can
     * show the seller which field is missing, where a parse failure would surface as a 500 on the list.
     */
    @Test
    void aSectionWithNoMetaReadsAsEmptyRatherThanThrowing() {
        assertThat(SectionBinding.meta(section(null)).kind()).isNull();
    }

    @Test
    void populateResolvesTheImageUrlSoTheConsoleNeedsNoSecondCall() {
        when(media.url(any(), anyLong())).thenReturn(Optional.of(IMAGE_URL));
        ReadableSection dto = new ReadableSection();

        binding.populate(section(meta(HomeSectionKind.IMAGE, null, 42L)), dto);

        assertThat(dto.getKind()).isEqualTo(HomeSectionKind.IMAGE);
        assertThat(dto.getMediaId()).isEqualTo(42L);
        assertThat(dto.getImageUrl()).isEqualTo(IMAGE_URL);
    }

    @Test
    void aSectionWithNoImageDoesNotAskTheMediaLibraryForOne() {
        ReadableSection dto = new ReadableSection();

        binding.populate(section(meta(HomeSectionKind.PRODUCT_GROUP, FEATURED, null)), dto);

        assertThat(dto.getImageUrl()).isNull();
        verifyNoInteractions(media);
    }

    @Test
    void theSubtitleNamesTheKindAndWhatItPointsAt() {
        assertThat(binding.subtitle(section(meta(HomeSectionKind.PRODUCT_GROUP, FEATURED, null)), ContentFixtures.EN))
                .isEqualTo("product group · FEATURED_ITEMS");
    }

    @Test
    void aSectionThatPointsAtNothingIsSubtitledByItsKindAlone() {
        assertThat(binding.subtitle(section(meta(HomeSectionKind.RICH_TEXT, "  ", null)), ContentFixtures.EN))
                .isEqualTo("rich text");
        assertThat(binding.subtitle(section(null), ContentFixtures.EN)).isEqualTo("—");
    }

    @Test
    void aSectionWithNoKindIsRefusedAndNothingElseIsReported() {
        List<FieldError> problems = binding.publishProblems(section(null), null);

        // Only the kind: every other check reads the kind, so reporting them too would be guesswork.
        assertThat(problems).singleElement().extracting(FieldError::field).isEqualTo(KIND_FIELD);
    }

    @Test
    void theCollectingKindsAreRefusedWithoutSomethingToCollect() {
        for (HomeSectionKind kind : HomeSectionKind.values()) {
            List<FieldError> problems = binding.publishProblems(section(meta(kind, null, 1L)), null);
            assertThat(problems.stream().map(FieldError::field))
                    .as("%s", kind)
                    .containsExactlyElementsOf(kind.needsTarget() ? List.of(TARGET_FIELD) : List.of());
        }
    }

    @Test
    void aBlankTargetCountsAsNoTarget() {
        assertThat(binding.publishProblems(section(meta(HomeSectionKind.BANNER_REF, "   ", null)), null))
                .singleElement().extracting(FieldError::field).isEqualTo(TARGET_FIELD);
    }

    @Test
    void anImageSectionIsRefusedWithoutAnImage() {
        assertThat(binding.publishProblems(section(meta(HomeSectionKind.IMAGE, null, null)), null))
                .singleElement().extracting(FieldError::field).isEqualTo(MEDIA_FIELD);
    }

    @Test
    void aCompleteSectionHasNothingToReport() {
        assertThat(binding.publishProblems(section(meta(HomeSectionKind.PRODUCT_GROUP, FEATURED, null)), null))
                .isEmpty();
    }

    /**
     * What the media library counts as a use of the asset, so an image a section draws cannot be deleted from
     * under it.
     */
    @Test
    void anImageSectionReportsItsAssetAsInUse() {
        assertThat(binding.mediaReferences(section(meta(HomeSectionKind.IMAGE, null, 42L))))
                .isEqualTo(Map.of("image", 42L));
        assertThat(binding.mediaReferences(section(meta(HomeSectionKind.PRODUCT_GROUP, FEATURED, null)))).isEmpty();
    }

}
