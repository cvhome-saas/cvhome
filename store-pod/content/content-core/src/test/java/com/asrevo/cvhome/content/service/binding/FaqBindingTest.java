package com.asrevo.cvhome.content.service.binding;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.model.faq.FaqMeta;
import com.asrevo.cvhome.content.model.faq.PersistableFaq;
import com.asrevo.cvhome.content.model.faq.ReadableFaq;
import com.asrevo.cvhome.content.service.FaqService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * FAQ entries.
 *
 * <p>
 * The row subtitle is deliberately "group · #3" rather than "position 3": it is rendered as-is in a console that
 * may be Arabic, and an English word baked in here could not be translated there.
 * </p>
 */
class FaqBindingTest {

    private static final String KEYWORD = "refund";

    private static final String AR = "ar";

    private static final String ARABIC_RETURNS = "الإرجاع";

    private static final String UNGROUPED_SUBTITLE = "— · #1";

    private static final String SLUG = "how-do-i-return";

    private static final Long GROUP_ID = 4L;

    private FaqService faq;

    private FaqBinding binding;

    @BeforeEach
    void setUp() {
        faq = mock(FaqService.class);
        binding = new FaqBinding(faq);
    }

    private static FaqGroup group(Map<String, String> names) {
        FaqGroup g = new FaqGroup();
        g.setId(GROUP_ID);
        g.setKey("returns");
        g.setNames(names == null ? null : JsonCodec.write(names));
        return g;
    }

    @Test
    void theTypeContractIsTheFaqOne() {
        assertThat(binding.type()).isEqualTo(ContentType.FAQ);
        assertThat(binding.persistableClass()).isEqualTo(PersistableFaq.class);
        assertThat(binding.newReadable()).isInstanceOf(ReadableFaq.class);
    }

    @Test
    void anEntryWithoutAGroupLandsInTheStoresDefaultOne() {
        Content c = ContentFixtures.content(1L, ContentType.FAQ, SLUG);
        when(faq.defaultGroupId(ContentFixtures.STORE)).thenReturn(GROUP_ID);

        binding.apply(c, new PersistableFaq());

        assertThat(c.getParentId()).isEqualTo(GROUP_ID);
    }

    @Test
    void keywordsAreTrimmedAndDeduplicatedAndThePositionIsKept() {
        Content c = ContentFixtures.content(1L, ContentType.FAQ, SLUG);
        c.setSortOrder(9);
        PersistableFaq dto = new PersistableFaq();
        dto.setGroupId(GROUP_ID);
        dto.setKeywords(List.of(" refund ", KEYWORD, "  "));
        dto.setShowInCheckoutHelp(true);

        binding.apply(c, dto);

        assertThat(FaqBinding.meta(c).keywords()).containsExactly(KEYWORD);
        assertThat(FaqBinding.meta(c).showInCheckoutHelp()).isTrue();
        assertThat(c.getSortOrder()).isEqualTo(9);
    }

    @Test
    void aRowWithoutMetaReadsAsAnEmptyFaqMeta() {
        FaqMeta meta = FaqBinding.meta(ContentFixtures.content(1L, ContentType.FAQ, SLUG));

        assertThat(meta.keywords()).isEmpty();
        assertThat(meta.showInCheckoutHelp()).isFalse();
    }

    @Test
    void populateNamesTheGroupInWhateverLocaleIsStored() {
        Content c = ContentFixtures.published(1L, ContentType.FAQ, SLUG, "How do I return?");
        c.setParentId(GROUP_ID);
        c.setSortOrder(2);
        when(faq.byIds(ContentFixtures.STORE)).thenReturn(Map.of(GROUP_ID, group(Map.of(AR, ARABIC_RETURNS))));
        ReadableFaq dto = new ReadableFaq();

        binding.populate(c, dto);

        assertThat(dto.getGroupId()).isEqualTo(GROUP_ID);
        assertThat(dto.getPosition()).isEqualTo(2);
        assertThat(dto.getGroupName()).isEqualTo(ARABIC_RETURNS);
    }

    @Test
    void theSubtitleUsesTheAskedLocaleAndAOneBasedPosition() {
        Content c = ContentFixtures.content(1L, ContentType.FAQ, SLUG);
        c.setParentId(GROUP_ID);
        c.setSortOrder(2);
        when(faq.byIds(ContentFixtures.STORE))
                .thenReturn(Map.of(GROUP_ID, group(Map.of("en", "Returns", AR, ARABIC_RETURNS))));

        assertThat(binding.subtitle(c, ContentFixtures.AR)).isEqualTo("الإرجاع · #3");
        assertThat(binding.subtitle(c, ContentFixtures.EN)).isEqualTo("Returns · #3");
    }

    @Test
    void anEntryWithoutAGroupOrWithAMissingOneShowsADash() {
        Content ungrouped = ContentFixtures.content(1L, ContentType.FAQ, SLUG);
        Content orphan = ContentFixtures.content(2L, ContentType.FAQ, SLUG);
        orphan.setParentId(99L);
        when(faq.byIds(ContentFixtures.STORE)).thenReturn(Map.of());

        assertThat(binding.subtitle(ungrouped, ContentFixtures.EN)).isEqualTo(UNGROUPED_SUBTITLE);
        assertThat(binding.subtitle(orphan, ContentFixtures.EN)).isEqualTo(UNGROUPED_SUBTITLE);
    }

    @Test
    void aGroupWithoutAnyStoredNameFallsBackToItsKey() {
        Content c = ContentFixtures.content(1L, ContentType.FAQ, SLUG);
        c.setParentId(GROUP_ID);
        when(faq.byIds(ContentFixtures.STORE)).thenReturn(Map.of(GROUP_ID, group(Map.of())));

        assertThat(binding.subtitle(c, ContentFixtures.EN)).isEqualTo("returns · #1");
    }

}
