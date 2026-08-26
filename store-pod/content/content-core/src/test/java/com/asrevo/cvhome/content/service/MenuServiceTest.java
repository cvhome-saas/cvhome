package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.Menu;
import com.asrevo.cvhome.content.entity.MenuItem;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.MenuTargetKind;
import com.asrevo.cvhome.content.model.menu.MenuTarget;
import com.asrevo.cvhome.content.model.storefront.StorefrontMenuNode;
import com.asrevo.cvhome.content.model.summary.ContentSummary;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.MenuRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Storefront navigation: two menus, one level of nesting, replaced whole. The MAIN menu is bootstrapped from the
 * pages flagged {@code showInFooter}, so a store that never opened the editor still has a footer.
 */
class MenuServiceTest {

    private static final String EN = "en";

    private static final String MAIN_MENU_NAME = "Main menu";

    private static final String SALE_PATH = "/sale";

    private static final String PATH_A = "/a";

    private static final String PATH_B = "/b";

    private static final String NEW_MENU_NAME = "Navigation";

    private static final String DRAFT_SLUG = "draft";

    private static final String STORE_ID = ContentFixtures.STORE.getId();

    private static final String ABOUT = "about";

    private static final String LABEL = "About";

    private MenuRepository menus;

    private ContentRepository contents;

    private MenuService service;

    @BeforeEach
    void setUp() {
        menus = mock(MenuRepository.class);
        contents = mock(ContentRepository.class);
        service = new MenuService(menus, contents);
    }

    private static Menu menu(Long id, MenuHandle handle) {
        Menu m = new Menu();
        m.setId(id);
        m.setStoreMerchantId(STORE_ID);
        m.setHandle(handle);
        m.setNames(JsonCodec.write(Map.of(EN, MAIN_MENU_NAME)));
        return m;
    }

    private static MenuItem item(Long id, Menu menu, Long parentId, MenuTargetKind kind, String value, int position) {
        MenuItem i = new MenuItem();
        i.setId(id);
        i.setMenu(menu);
        i.setParentId(parentId);
        i.setPosition(position);
        i.setTargetKind(kind);
        i.setTargetValue(value);
        i.setLabels(JsonCodec.write(Map.of(EN, LABEL)));
        return i;
    }

    /**
     * Stands in for the id generator: {@code put} flushes so the parent row has an id before its children point
     * at it, and the children lookup keys on that id.
     */
    private void assignIdsOnFlush(Menu m) {
        org.mockito.Mockito.doAnswer(invocation -> {
            long next = 100L;
            for (MenuItem i : m.getItems()) {
                if (i.getId() == null) {
                    i.setId(next++);
                }
            }
            return null;
        }).when(menus).flush();
    }

    private static com.asrevo.cvhome.content.model.menu.MenuItem dto(MenuTargetKind kind, String value) {
        var d = new com.asrevo.cvhome.content.model.menu.MenuItem();
        d.setTarget(new MenuTarget(kind, value, null));
        d.setLabels(Map.of(EN, LABEL));
        return d;
    }

    @ParameterizedTest
    @CsvSource({
        "PAGE, about, /content/about",
        "CATEGORY, shoes, /category/shoes",
        "PRODUCT, sku-1, /product/sku-1",
        "POLICY, TERMS, /policies/terms",
        "BLOG_INDEX, , /blog",
        "FAQ_INDEX, , /help",
        "URL, https://example.test, https://example.test",
    })
    void everyTargetKindHasItsOwnStorefrontPath(MenuTargetKind kind, String value, String expected) {
        assertThat(MenuService.href(kind, value)).isEqualTo(expected);
    }

    @Test
    void aPolicyTargetWithoutAValueStillProducesAPath() {
        assertThat(MenuService.href(MenuTargetKind.POLICY, null)).isEqualTo("/policies/");
    }

    @Test
    void bothMenusAreListedAndTheFooterIsBootstrappedOnFirstRead() {
        Content page = ContentFixtures.published(1L, ContentType.PAGE, ABOUT, LABEL);
        Content footerPage = ContentFixtures.published(2L, ContentType.PAGE, "terms", "Terms");
        footerPage.setShowInFooter(true);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.empty());
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.FOOTER)).thenReturn(Optional.empty());
        when(contents.findVisibleByType(ContentFixtures.STORE, ContentType.PAGE))
                .thenReturn(List.of(page, footerPage));
        when(menus.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));

        var out = service.list(ContentFixtures.STORE);

        assertThat(out).extracting("handle").containsExactly(MenuHandle.MAIN, MenuHandle.FOOTER);
        // MAIN starts empty: it used to be seeded from a legacy link_to_menu column only the retired seller UI
        // could set, so the menu a seller saw was built from data they had no way to edit.
        assertThat(out.getFirst().getItemCount()).isZero();
        assertThat(out.getLast().getItemCount()).isEqualTo(1);
    }

    @Test
    void anExistingMenuIsNotBootstrappedAgain() {
        Menu m = menu(1L, MenuHandle.MAIN);
        m.getItems().add(item(10L, m, null, MenuTargetKind.PAGE, ABOUT, 0));
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());

        var dtoMenu = service.get(ContentFixtures.STORE, MenuHandle.MAIN);

        assertThat(dtoMenu.getNames()).containsEntry(EN, MAIN_MENU_NAME);
        assertThat(dtoMenu.getItems()).singleElement()
                .satisfies(i -> assertThat(i.getTarget().broken()).isTrue());
    }

    @Test
    void anInternalTargetThatResolvesIsNotFlaggedBroken() {
        Menu m = menu(1L, MenuHandle.MAIN);
        m.getItems().add(item(10L, m, null, MenuTargetKind.PAGE, ABOUT, 0));
        m.getItems().add(item(11L, m, 10L, MenuTargetKind.URL, SALE_PATH, 0));
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE))
                .thenReturn(List.of(ContentFixtures.published(1L, ContentType.PAGE, ABOUT, LABEL)));

        var dtoMenu = service.get(ContentFixtures.STORE, MenuHandle.MAIN);

        assertThat(dtoMenu.getItems()).singleElement().satisfies(i -> {
            assertThat(i.getTarget().broken()).isFalse();
            assertThat(i.getChildren()).singleElement()
                    .satisfies(child -> assertThat(child.getTarget().broken()).isFalse());
        });
    }

    @Test
    void replacingTheTreeNormalisesPositionsPerLevel() throws Exception {
        Menu m = menu(1L, MenuHandle.MAIN);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());
        assignIdsOnFlush(m);
        var parent = dto(MenuTargetKind.PAGE, ABOUT);
        parent.getChildren().add(dto(MenuTargetKind.URL, PATH_A));
        parent.getChildren().add(dto(MenuTargetKind.URL, PATH_B));
        var body = new com.asrevo.cvhome.content.model.menu.Menu();
        body.setNames(Map.of(EN, NEW_MENU_NAME));
        body.getItems().add(parent);
        body.getItems().add(dto(MenuTargetKind.BLOG_INDEX, null));

        service.put(ContentFixtures.STORE, MenuHandle.MAIN, body);

        assertThat(m.getItems()).hasSize(4);
        assertThat(m.getItems()).extracting(MenuItem::getPosition).containsExactly(0, 0, 1, 1);
        assertThat(JsonCodec.read(m.getNames(), java.util.LinkedHashMap.class)).containsEntry(EN, NEW_MENU_NAME);
    }

    @Test
    void anEmptyNamesMapLeavesTheStoredOneAlone() throws Exception {
        Menu m = menu(1L, MenuHandle.MAIN);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());
        assignIdsOnFlush(m);

        service.put(ContentFixtures.STORE, MenuHandle.MAIN, new com.asrevo.cvhome.content.model.menu.Menu());

        assertThat(JsonCodec.read(m.getNames(), java.util.LinkedHashMap.class)).containsEntry(EN, MAIN_MENU_NAME);
    }

    @Test
    void nestingTwoLevelsDeepIsRefused() {
        Menu m = menu(1L, MenuHandle.MAIN);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        var grandchild = dto(MenuTargetKind.URL, "/deep");
        var child = dto(MenuTargetKind.URL, PATH_A);
        child.getChildren().add(grandchild);
        var parent = dto(MenuTargetKind.PAGE, ABOUT);
        parent.getChildren().add(child);
        var body = new com.asrevo.cvhome.content.model.menu.Menu();
        body.getItems().add(parent);

        assertThatThrownBy(() -> service.put(ContentFixtures.STORE, MenuHandle.MAIN, body))
                .isInstanceOf(ContentRuleException.class);
    }

    @Test
    void anItemWithoutATargetKindIsRejected() {
        Menu m = menu(1L, MenuHandle.MAIN);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        var item = new com.asrevo.cvhome.content.model.menu.MenuItem();
        var body = new com.asrevo.cvhome.content.model.menu.Menu();
        body.getItems().add(item);

        assertThatThrownBy(() -> service.put(ContentFixtures.STORE, MenuHandle.MAIN, body))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("target kind");
    }

    @Test
    void aRelativeUrlTargetMustStartWithAKnownPrefix() {
        Menu m = menu(1L, MenuHandle.MAIN);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        var body = new com.asrevo.cvhome.content.model.menu.Menu();
        body.getItems().add(dto(MenuTargetKind.URL, "sale"));

        assertThatThrownBy(() -> service.put(ContentFixtures.STORE, MenuHandle.MAIN, body))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("absolute or start with /");
    }

    @Test
    void aPageTargetWithoutASlugIsRejected() {
        Menu m = menu(1L, MenuHandle.MAIN);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        var body = new com.asrevo.cvhome.content.model.menu.Menu();
        body.getItems().add(dto(MenuTargetKind.PAGE, "  "));

        assertThatThrownBy(() -> service.put(ContentFixtures.STORE, MenuHandle.MAIN, body))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("need a value");
    }

    @Test
    void anIndexTargetNeedsNoValueAtAll() throws Exception {
        Menu m = menu(1L, MenuHandle.FOOTER);
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.FOOTER)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());
        assignIdsOnFlush(m);
        var body = new com.asrevo.cvhome.content.model.menu.Menu();
        body.getItems().add(dto(MenuTargetKind.FAQ_INDEX, null));

        assertThat(service.put(ContentFixtures.STORE, MenuHandle.FOOTER, body).getItems()).hasSize(1);
    }

    @Test
    void theResolvedTreeDropsHiddenItemsAndUnservablePages() {
        Menu m = menu(1L, MenuHandle.MAIN);
        MenuItem visible = item(10L, m, null, MenuTargetKind.PAGE, ABOUT, 0);
        MenuItem hidden = item(11L, m, null, MenuTargetKind.URL, "/hidden", 1);
        hidden.setVisible(false);
        MenuItem draftPage = item(12L, m, null, MenuTargetKind.PAGE, DRAFT_SLUG, 2);
        MenuItem hiddenChild = item(13L, m, 10L, MenuTargetKind.URL, "/c", 0);
        hiddenChild.setVisible(false);
        MenuItem child = item(14L, m, 10L, MenuTargetKind.URL, SALE_PATH, 1);
        m.getItems().addAll(List.of(visible, hidden, draftPage, hiddenChild, child));
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE))
                .thenReturn(List.of(ContentFixtures.published(1L, ContentType.PAGE, ABOUT, LABEL),
                        ContentFixtures.content(2L, ContentType.PAGE, DRAFT_SLUG)));

        List<StorefrontMenuNode> nodes = service.resolved(ContentFixtures.STORE, MenuHandle.MAIN, ContentFixtures.EN,
                ContentFixtures.NOW);

        assertThat(nodes).singleElement().satisfies(n -> {
            assertThat(n.getHref()).isEqualTo("/content/about");
            assertThat(n.getLabel()).isEqualTo(LABEL);
            assertThat(n.getChildren()).extracting(StorefrontMenuNode::getValue).containsExactly(SALE_PATH);
        });
    }

    @Test
    void aLabelMissingInTheAskedLocaleFallsBackToAnyOtherThenTheTargetValue() {
        Menu m = menu(1L, MenuHandle.FOOTER);
        MenuItem labelled = item(10L, m, null, MenuTargetKind.URL, PATH_A, 0);
        MenuItem unlabelled = item(11L, m, null, MenuTargetKind.URL, PATH_B, 1);
        unlabelled.setLabels(null);
        m.getItems().addAll(List.of(labelled, unlabelled));
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.FOOTER)).thenReturn(Optional.of(m));
        when(contents.findAllByType(ContentFixtures.STORE, ContentType.PAGE)).thenReturn(List.of());

        List<StorefrontMenuNode> nodes = service.resolved(ContentFixtures.STORE, MenuHandle.FOOTER,
                ContentFixtures.AR, ContentFixtures.NOW);

        assertThat(nodes).extracting(StorefrontMenuNode::getLabel).containsExactly(LABEL, PATH_B);
    }

    @Test
    void theSummaryCountsBothMenus() {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();

        service.contribute(ContentFixtures.STORE, new ContentSummary(), counts);

        assertThat(counts).containsEntry("menus", 2L);
    }

    @Test
    void findReturnsWhatTheRepositoryHolds() {
        when(menus.findByStoreAndHandle(STORE_ID, MenuHandle.MAIN)).thenReturn(Optional.empty());

        assertThat(service.find(ContentFixtures.STORE, MenuHandle.MAIN)).isEmpty();
    }

}
