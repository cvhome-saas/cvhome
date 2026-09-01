package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.SectionPreset;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.layout.LayoutItem;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.PersistableSavedSection;
import com.asrevo.cvhome.content.model.layout.SavedSection;
import com.asrevo.cvhome.content.repository.SectionPresetRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The merchant's section library: snapshots in, snapshots out, with the caps that keep it a library —
 * a known kind, bounded items, a bounded snapshot, and a bounded shelf.
 */
class SectionPresetServiceTest {

    private static final String HERO = "hero";

    private static final String NAME = "My hero";

    private static final String ACTOR = "tester";

    private static final String HEADING = "heading";

    private static final String EN = "en";

    private static final String WELCOME = "Welcome";

    private SectionPresetRepository presets;

    private SectionPresetService service;

    @BeforeEach
    void setUp() {
        presets = mock(SectionPresetRepository.class);
        when(presets.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new SectionPresetService(presets,
                Clock.fixed(Instant.parse("2026-08-31T12:00:00Z"), ZoneOffset.UTC));
    }

    private static LayoutSection section(String kind, List<LayoutItem> items) {
        return new LayoutSection("sec-1", kind, "minimal", Map.of(), items,
                Map.of(HEADING, Map.of(EN, WELCOME)), null, null, null, null);
    }

    @Test
    void savesASnapshotAndReadsItBack() throws Exception {
        SavedSection saved = service.save(ContentFixtures.STORE,
                new PersistableSavedSection(NAME, section(HERO, null)), ACTOR);

        assertThat(saved.name()).isEqualTo(NAME);
        assertThat(saved.kind()).isEqualTo(HERO);
        assertThat(saved.section().text().get(HEADING).get(EN)).isEqualTo(WELCOME);
        verify(presets).save(any());
    }

    @Test
    void refusesAKindNothingRenders() {
        assertThatThrownBy(() -> service.save(ContentFixtures.STORE,
                new PersistableSavedSection(NAME, section("no-such-kind", null)), ACTOR))
                .isInstanceOf(InvalidContentRequestException.class);
    }

    @Test
    void refusesMoreItemsThanASectionMayHold() {
        List<LayoutItem> items = IntStream.rangeClosed(0, LayoutSection.MAX_ITEMS)
                .mapToObj(i -> new LayoutItem(String.format("itm-%d", i), Map.of(), Map.of()))
                .toList();
        assertThatThrownBy(() -> service.save(ContentFixtures.STORE,
                new PersistableSavedSection(NAME, section(HERO, items)), ACTOR))
                .isInstanceOf(InvalidContentRequestException.class);
    }

    @Test
    void refusesAFullShelf() {
        when(presets.countByStoreMerchantId(ContentFixtures.STORE.getId())).thenReturn(100L);
        assertThatThrownBy(() -> service.save(ContentFixtures.STORE,
                new PersistableSavedSection(NAME, section(HERO, null)), ACTOR))
                .isInstanceOf(InvalidContentRequestException.class);
    }

    @Test
    void listsAndDeletesOnlyTheStoresOwnRows() throws Exception {
        SectionPreset row = new SectionPreset();
        row.setId(7L);
        row.setStoreMerchantId(ContentFixtures.STORE.getId());
        row.setName(NAME);
        row.setKind(HERO);
        row.setSnapshot("{\"id\":\"sec-1\",\"kind\":\"hero\"}");
        when(presets.findByStoreMerchantIdOrderByDateCreatedDesc(ContentFixtures.STORE.getId()))
                .thenReturn(List.of(row));
        when(presets.findByIdAndStoreMerchantId(7L, ContentFixtures.STORE.getId()))
                .thenReturn(Optional.of(row));

        assertThat(service.list(ContentFixtures.STORE)).singleElement()
                .satisfies(saved -> assertThat(saved.id()).isEqualTo(7L));
        service.delete(ContentFixtures.STORE, 7L);
        verify(presets).delete(row);

        assertThatThrownBy(() -> service.delete(ContentFixtures.STORE, 8L))
                .isInstanceOf(ContentNotFoundException.class);
    }

}
