package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.faq.FaqReorder;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.FaqGroupRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FAQ groups and entry ordering.
 *
 * <p>
 * The seed carries both platform languages on purpose: an Arabic console used to read "General" because the
 * starter groups were English-only and the name is shown as stored.
 * </p>
 */
class FaqServiceTest {

    private static final String EN = "en";

    private static final String RETURNS_KEY = "returns";

    private static final String RETURNS_NAME = "Returns";

    private static final String STORE_ID = ContentFixtures.STORE.getId();

    private static final String GENERAL = "general";

    private static final String SHIPPING = "shipping";

    private FaqGroupRepository groups;

    private ContentRepository contents;

    private FaqService service;

    @BeforeEach
    void setUp() {
        groups = mock(FaqGroupRepository.class);
        contents = mock(ContentRepository.class);
        service = new FaqService(groups, contents);
    }

    private static FaqGroup group(Long id, String key, int position) {
        FaqGroup g = new FaqGroup();
        g.setId(id);
        g.setStoreMerchantId(STORE_ID);
        g.setKey(key);
        g.setPosition(position);
        g.setNames(JsonCodec.write(Map.of(EN, key)));
        return g;
    }

    private static Content entry(Long id, Long groupId, Integer sortOrder) {
        Content c = ContentFixtures.content(id, ContentType.FAQ, String.format("q-%d", id));
        c.setParentId(groupId);
        c.setSortOrder(sortOrder);
        return c;
    }

    @Test
    void aStoreWithNoGroupsIsSeededInBothPlatformLanguages() {
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of()).thenReturn(List.of(group(1L, GENERAL, 0)));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ)).thenReturn(List.of());

        service.groups(ContentFixtures.STORE);

        var captor = org.mockito.ArgumentCaptor.forClass(FaqGroup.class);
        verify(groups, times(4)).save(captor.capture());
        assertThat(FaqService.names(captor.getAllValues().getFirst()))
                .containsEntry(EN, "General").containsEntry("ar", "عام");
    }

    @Test
    void groupsCarryTheirEntryCounts() {
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(group(1L, GENERAL, 0), group(2L, SHIPPING, 1)));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ))
                .thenReturn(List.of(entry(10L, 1L, 0), entry(11L, 1L, 1), entry(12L, null, 0)));

        assertThat(service.groups(ContentFixtures.STORE))
                .extracting("key", "entryCount")
                .containsExactly(org.assertj.core.groups.Tuple.tuple(GENERAL, 2L),
                        org.assertj.core.groups.Tuple.tuple(SHIPPING, 0L));
        verify(groups, never()).save(any());
    }

    @Test
    void aDuplicateGroupKeyIsAConflict() {
        when(groups.findByStoreMerchantIdAndKey(STORE_ID, GENERAL)).thenReturn(Optional.of(group(1L, GENERAL, 0)));
        var body = new com.asrevo.cvhome.content.model.faq.FaqGroup();
        body.setKey(GENERAL);

        assertThatThrownBy(() -> service.create(ContentFixtures.STORE, body))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void aNewGroupWithoutAPositionSortsFirst() throws Exception {
        when(groups.findByStoreMerchantIdAndKey(STORE_ID, RETURNS_KEY)).thenReturn(Optional.empty());
        when(groups.saveAndFlush(any())).thenAnswer(i -> {
            FaqGroup g = i.getArgument(0);
            g.setId(9L);
            return g;
        });
        var body = new com.asrevo.cvhome.content.model.faq.FaqGroup();
        body.setKey(RETURNS_KEY);
        body.setNames(Map.of(EN, RETURNS_NAME));

        var out = service.create(ContentFixtures.STORE, body);

        assertThat(out.getPosition()).isZero();
        assertThat(out.getEntryCount()).isZero();
        assertThat(out.getNames()).containsEntry(EN, RETURNS_NAME);
    }

    @Test
    void updatingAGroupOfAnotherStoreReadsAsMissing() {
        when(groups.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(ContentFixtures.STORE, 1L,
                new com.asrevo.cvhome.content.model.faq.FaqGroup()))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void renamingAGroupOntoAnotherKeyThatExistsIsAConflict() {
        when(groups.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.of(group(1L, GENERAL, 0)));
        when(groups.findByStoreMerchantIdAndKey(STORE_ID, SHIPPING)).thenReturn(Optional.of(group(2L, SHIPPING, 1)));
        var body = new com.asrevo.cvhome.content.model.faq.FaqGroup();
        body.setKey(SHIPPING);

        assertThatThrownBy(() -> service.update(ContentFixtures.STORE, 1L, body))
                .isInstanceOf(ContentConflictException.class);
    }

    @Test
    void keepingTheKeyIsNotADuplicateOfItself() throws Exception {
        when(groups.findByIdAndStoreMerchantId(1L, STORE_ID)).thenReturn(Optional.of(group(1L, GENERAL, 0)));
        when(groups.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ))
                .thenReturn(List.of(entry(10L, 1L, 0)));
        var body = new com.asrevo.cvhome.content.model.faq.FaqGroup();
        body.setKey(GENERAL);
        body.setNames(Map.of(EN, "General questions"));
        body.setPosition(3);

        var out = service.update(ContentFixtures.STORE, 1L, body);

        assertThat(out.getPosition()).isEqualTo(3);
        assertThat(out.getEntryCount()).isEqualTo(1L);
    }

    @Test
    void deletingAGroupMovesItsEntriesToTheFirstRemainingOne() throws Exception {
        FaqGroup doomed = group(2L, SHIPPING, 1);
        Content orphan = entry(10L, 2L, 0);
        when(groups.findByIdAndStoreMerchantId(2L, STORE_ID)).thenReturn(Optional.of(doomed));
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(group(1L, GENERAL, 0)));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ))
                .thenReturn(List.of(orphan, entry(11L, 1L, 0)));

        service.delete(ContentFixtures.STORE, 2L);

        verify(groups).delete(doomed);
        assertThat(orphan.getParentId()).isEqualTo(1L);
        verify(contents, times(1)).save(orphan);
    }

    @Test
    void deletingAGroupOfAnotherStoreReadsAsMissing() {
        when(groups.findByIdAndStoreMerchantId(2L, STORE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(ContentFixtures.STORE, 2L))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void reorderMovesEntriesAndRenumbersEveryTouchedGroup() throws Exception {
        Content first = entry(10L, 1L, 0);
        Content second = entry(11L, 1L, 1);
        Content third = entry(12L, 2L, 0);
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ))
                .thenReturn(List.of(first, second, third));
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(group(1L, GENERAL, 0), group(2L, SHIPPING, 1)));

        service.reorder(ContentFixtures.STORE, List.of(reorder(12L, 1L, 0), reorder(10L, 1L, 5)));

        assertThat(third.getParentId()).isEqualTo(1L);
        assertThat(third.getSortOrder()).isZero();
        assertThat(second.getSortOrder()).isEqualTo(1);
        assertThat(first.getSortOrder()).isEqualTo(2);
    }

    @Test
    void reorderingAnEntryThisStoreDoesNotOwnReadsAsMissing() {
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ)).thenReturn(List.of());
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(group(1L, GENERAL, 0)));

        assertThatThrownBy(() -> service.reorder(ContentFixtures.STORE, List.of(reorder(99L, 1L, 0))))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void reorderingIntoAGroupThatIsNotThisStoresReadsAsMissing() {
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.FAQ))
                .thenReturn(List.of(entry(10L, 1L, 0)));
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(group(1L, GENERAL, 0)));

        assertThatThrownBy(() -> service.reorder(ContentFixtures.STORE, List.of(reorder(10L, 77L, 0))))
                .isInstanceOf(ContentNotFoundException.class);
    }

    @Test
    void theDefaultGroupIsTheFirstOneAndIsCreatedOnDemand() {
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of()).thenReturn(List.of(group(1L, GENERAL, 0)));

        assertThat(service.defaultGroupId(ContentFixtures.STORE)).isEqualTo(1L);
    }

    @Test
    void byIdsKeysTheGroupsByTheirId() {
        when(groups.findByStoreMerchantIdOrderByPositionAscIdAsc(STORE_ID))
                .thenReturn(List.of(group(1L, GENERAL, 0), group(2L, SHIPPING, 1)));

        assertThat(service.byIds(ContentFixtures.STORE)).containsOnlyKeys(1L, 2L);
    }

    @Test
    void aGroupWithoutStoredNamesReadsAsAnEmptyMap() {
        FaqGroup g = group(1L, GENERAL, 0);
        g.setNames(null);

        assertThat(FaqService.names(g)).isEmpty();
    }

    private static FaqReorder reorder(Long id, Long groupId, Integer position) {
        FaqReorder m = new FaqReorder();
        m.setId(id);
        m.setGroupId(groupId);
        m.setPosition(position);
        return m;
    }

}
