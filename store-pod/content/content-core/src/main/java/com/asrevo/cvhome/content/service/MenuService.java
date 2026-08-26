package com.asrevo.cvhome.content.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.Menu;
import com.asrevo.cvhome.content.entity.MenuItem;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.MenuHandle;
import com.asrevo.cvhome.content.model.MenuTargetKind;
import com.asrevo.cvhome.content.model.menu.MenuTarget;
import com.asrevo.cvhome.content.model.storefront.StorefrontMenuNode;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.MenuRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.content.support.Strings;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Storefront navigation: two menus per store (MAIN, FOOTER), one level of nesting, replaced whole by the editor.
 *
 * <p>
 * FOOTER is bootstrapped on first read from the pages flagged {@code showInFooter}, so a store that never opened
 * the editor still has a footer. MAIN starts empty: it used to be seeded from a legacy {@code linkToMenu} column
 * that only the retired seller UI could set, which meant the menu a seller saw depended on data they had no way
 * to edit.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MenuService implements SummaryService.MediaFigures {

    private static final List<String> URL_PREFIXES = List.of("/", "http://", "https://", "mailto:", "tel:");

    private final MenuRepository menus;

    private final ContentRepository contents;

    // --------------------------------------------------------------------------------------------- console

    @Transactional(rollbackFor = Exception.class)
    public List<com.asrevo.cvhome.content.model.menu.Menu> list(StoreMerchantId store) {
        List<com.asrevo.cvhome.content.model.menu.Menu> out = new ArrayList<>();
        for (MenuHandle handle : MenuHandle.values()) {
            out.add(toDto(ensure(store, handle), store, false));
        }
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    public com.asrevo.cvhome.content.model.menu.Menu get(StoreMerchantId store, MenuHandle handle) {
        return toDto(ensure(store, handle), store, true);
    }

    /**
     * Replaces the whole tree. Positions are normalised 0..n per level; depth beyond one child level is a 422;
     * internal targets that do not resolve are flagged {@code broken} in the response but saved.
     */
    @Transactional(rollbackFor = Exception.class)
    public com.asrevo.cvhome.content.model.menu.Menu put(StoreMerchantId store, MenuHandle handle,
                                                         com.asrevo.cvhome.content.model.menu.Menu body)
            throws ContentRuleException, InvalidContentRequestException {
        Menu menu = ensure(store, handle);
        if (body.getNames() != null && !body.getNames().isEmpty()) {
            menu.setNames(JsonCodec.write(body.getNames()));
        }
        // `menu` is managed; plain flushes persist the new rows in place (saveAndFlush would merge copies and
        // leave our local references without ids)
        menu.getItems().clear();
        menus.flush();
        int position = 0;
        for (com.asrevo.cvhome.content.model.menu.MenuItem item : body.getItems()) {
            MenuItem parent = row(menu, null, item, position++);
            menu.getItems().add(parent);
            menus.flush();
            int childPosition = 0;
            for (com.asrevo.cvhome.content.model.menu.MenuItem child : item.getChildren()) {
                if (!child.getChildren().isEmpty()) {
                    throw ContentRuleException.menuDepth(handle);
                }
                menu.getItems().add(row(menu, parent.getId(), child, childPosition++));
            }
        }
        menus.flush();
        return toDto(menu, store, true);
    }

    // ------------------------------------------------------------------------------------------ storefront

    /**
     * The resolved tree for the storefront: visible items only, labels in {@code language} with fallback, hrefs
     * computed, broken internal links dropped.
     */
    @Transactional(rollbackFor = Exception.class)
    public List<StorefrontMenuNode> resolved(StoreMerchantId store, MenuHandle handle, LanguageCode language,
                                             Instant now) {
        Menu menu = ensure(store, handle);
        Map<String, Content> pages = pagesBySlug(store);
        List<StorefrontMenuNode> out = new ArrayList<>();
        for (MenuItem parent : roots(menu)) {
            if (!parent.isVisible()) {
                continue;
            }
            StorefrontMenuNode node = node(parent, language, pages, now);
            if (node == null) {
                continue;
            }
            for (MenuItem child : children(menu, parent.getId())) {
                if (!child.isVisible()) {
                    continue;
                }
                StorefrontMenuNode c = node(child, language, pages, now);
                if (c != null) {
                    node.getChildren().add(c);
                }
            }
            out.add(node);
        }
        return out;
    }

    @Override
    public void contribute(StoreMerchantId store, com.asrevo.cvhome.content.model.summary.ContentSummary summary,
                           Map<String, Long> counts) {
        counts.put("menus", (long) MenuHandle.values().length);
    }

    // --------------------------------------------------------------------------------------------- helpers

    private Menu ensure(StoreMerchantId store, MenuHandle handle) {
        return menus.findByStoreAndHandle(store.getId(), handle).orElseGet(() -> bootstrap(store, handle));
    }

    private Menu bootstrap(StoreMerchantId store, MenuHandle handle) {
        Menu menu = new Menu();
        menu.setStoreMerchantId(store.getId());
        menu.setHandle(handle);
        menu.setNames(JsonCodec.write(Map.of("en", handle == MenuHandle.MAIN ? "Main menu" : "Footer menu")));
        menu = menus.saveAndFlush(menu);
        int position = 0;
        for (Content page : contents.findVisibleByType(store, ContentType.PAGE)) {
            if (handle != MenuHandle.FOOTER || !page.isShowInFooter()) {
                continue;
            }
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setPosition(position++);
            Map<String, String> labels = new LinkedHashMap<>();
            page.getDescriptions().forEach(d -> labels.put(d.getLanguageCode().code(),
                    d.getTitle() != null ? d.getTitle() : d.getName()));
            item.setLabels(JsonCodec.write(labels));
            item.setTargetKind(MenuTargetKind.PAGE);
            item.setTargetValue(page.getCode());
            menu.getItems().add(item);
        }
        return menus.saveAndFlush(menu);
    }

    private static void validateTarget(MenuTarget target) throws InvalidContentRequestException {
        if (target == null || target.kind() == null) {
            throw InvalidContentRequestException.menuTargetInvalid("every item needs a target kind");
        }
        String v = target.value();
        boolean blank = Strings.blank(v);
        if (target.kind() == MenuTargetKind.URL) {
            if (blank || URL_PREFIXES.stream().noneMatch(v::startsWith)) {
                throw InvalidContentRequestException.menuTargetInvalid("URL targets must be absolute or start with /");
            }
            return;
        }
        boolean needsValue = target.kind() != MenuTargetKind.BLOG_INDEX && target.kind() != MenuTargetKind.FAQ_INDEX;
        if (needsValue && blank) {
            throw InvalidContentRequestException.menuTargetInvalid(
                    String.format("%s targets need a value", target.kind()));
        }
    }

    private MenuItem row(Menu menu, Long parentId, com.asrevo.cvhome.content.model.menu.MenuItem dto, int position)
            throws InvalidContentRequestException {
        validateTarget(dto.getTarget());
        MenuItem item = new MenuItem();
        item.setMenu(menu);
        item.setParentId(parentId);
        item.setPosition(position);
        item.setLabels(JsonCodec.write(dto.getLabels() == null ? Map.of() : dto.getLabels()));
        item.setTargetKind(dto.getTarget().kind());
        item.setTargetValue(Strings.trimToNull(dto.getTarget().value()));
        item.setOpenInNewTab(dto.isOpenInNewTab());
        item.setVisible(dto.isVisible());
        return item;
    }

    private com.asrevo.cvhome.content.model.menu.Menu toDto(Menu menu, StoreMerchantId store, boolean withItems) {
        var dto = new com.asrevo.cvhome.content.model.menu.Menu();
        dto.setId(menu.getId());
        dto.setHandle(menu.getHandle());
        dto.setNames(names(menu.getNames()));
        dto.setItemCount(menu.getItems().size());
        if (withItems) {
            Map<String, Content> pages = pagesBySlug(store);
            for (MenuItem parent : roots(menu)) {
                var p = itemDto(parent, pages);
                for (MenuItem child : children(menu, parent.getId())) {
                    p.getChildren().add(itemDto(child, pages));
                }
                dto.getItems().add(p);
            }
        }
        return dto;
    }

    private com.asrevo.cvhome.content.model.menu.MenuItem itemDto(MenuItem item, Map<String, Content> pages) {
        var d = new com.asrevo.cvhome.content.model.menu.MenuItem();
        d.setId(item.getId());
        d.setPosition(item.getPosition());
        d.setLabels(names(item.getLabels()));
        d.setTarget(new MenuTarget(item.getTargetKind(), item.getTargetValue(), broken(item, pages)));
        d.setOpenInNewTab(item.isOpenInNewTab());
        d.setVisible(item.isVisible());
        return d;
    }

    private static Boolean broken(MenuItem item, Map<String, Content> pages) {
        if (item.getTargetKind() != MenuTargetKind.PAGE) {
            return false;
        }
        Content page = pages.get(item.getTargetValue());
        return page == null || page.getStatus() != ContentStatus.PUBLISHED;
    }

    private StorefrontMenuNode node(MenuItem item, LanguageCode language, Map<String, Content> pages, Instant now) {
        if (item.getTargetKind() == MenuTargetKind.PAGE) {
            Content page = pages.get(item.getTargetValue());
            if (page == null || !page.servable(now)) {
                return null;
            }
        }
        StorefrontMenuNode n = new StorefrontMenuNode();
        Map<String, String> labels = names(item.getLabels());
        String label = language == null ? null : labels.get(language.code());
        if (label == null) {
            label = labels.values().stream().findFirst().orElse(item.getTargetValue());
        }
        n.setLabel(label);
        n.setKind(item.getTargetKind());
        n.setValue(item.getTargetValue());
        n.setHref(href(item.getTargetKind(), item.getTargetValue()));
        n.setOpenInNewTab(item.isOpenInNewTab());
        return n;
    }

    public static String href(MenuTargetKind kind, String value) {
        return switch (kind) {
            case PAGE -> String.format("/content/%s", value);
            case CATEGORY -> String.format("/category/%s", value);
            case PRODUCT -> String.format("/product/%s", value);
            case POLICY -> String.format("/policies/%s", value == null ? "" : value.toLowerCase());
            case BLOG_INDEX -> "/blog";
            case FAQ_INDEX -> "/help";
            case URL -> value;
        };
    }

    private Map<String, Content> pagesBySlug(StoreMerchantId store) {
        Map<String, Content> out = new LinkedHashMap<>();
        for (Content c : contents.findAllByType(store, ContentType.PAGE)) {
            out.put(c.getCode(), c);
        }
        return out;
    }

    private static List<MenuItem> roots(Menu menu) {
        return menu.getItems().stream().filter(i -> i.getParentId() == null)
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition())).toList();
    }

    private static List<MenuItem> children(Menu menu, Long parentId) {
        return menu.getItems().stream().filter(i -> parentId.equals(i.getParentId()))
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition())).toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> names(String json) {
        if (json == null) {
            return new LinkedHashMap<>();
        }
        Map<String, String> m = JsonCodec.read(json, LinkedHashMap.class);
        return m == null ? new LinkedHashMap<>() : m;
    }

    public Optional<Menu> find(StoreMerchantId store, MenuHandle handle) {
        return menus.findByStoreAndHandle(store.getId(), handle);
    }

}
