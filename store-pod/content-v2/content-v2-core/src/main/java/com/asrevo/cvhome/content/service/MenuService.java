package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.menu.ContentMenu;
import com.asrevo.cvhome.content.entity.menu.MenuItem;
import com.asrevo.cvhome.content.entity.menu.MenuItemDescription;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidMenuTreeException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.menu.MenuItemSpec;
import com.asrevo.cvhome.content.model.menu.MenuTargetKind;
import com.asrevo.cvhome.content.model.menu.MenuView;
import com.asrevo.cvhome.content.model.menu.MenuWriteRequest;
import com.asrevo.cvhome.content.repository.ContentMenuRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;

@Service
public class MenuService {
    private final ContentV2Service contentService;
    private final ContentRepository contentRepository;
    private final ContentMenuRepository menuRepository;

    public MenuService(ContentV2Service contentService, ContentRepository contentRepository,
                       ContentMenuRepository menuRepository) {
        this.contentService = contentService;
        this.contentRepository = contentRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional
    public MenuView create(StoreMerchantId store, LanguageCode language, MenuWriteRequest request, String actor)
            throws InvalidMenuTreeException, ContentNotFoundException {
        validate(request);
        ContentView contentView = contentService.create(store, language, request.content(), actor);
        Content content = contentRepository.findByIdAndStoreMerchantId(contentView.id(), store)
                .orElseThrow(() -> ContentNotFoundException.forId(contentView.id()));
        ContentMenu menu = new ContentMenu();
        menu.setContent(content);
        menu.setStoreMerchantId(store);
        menu.setHandle(request.handle());
        menu.replaceItems(buildItems(menu, language, request.items()));
        return toView(menuRepository.save(menu), contentView, language, store);
    }

    @Transactional
    public MenuView replace(StoreMerchantId store, LanguageCode language, Long id, MenuWriteRequest request)
            throws InvalidMenuTreeException, ContentNotFoundException {
        validate(request);
        ContentMenu menu = menuRepository.findByIdAndStoreMerchantId(id, store)
                .orElseThrow(() -> ContentNotFoundException.forId(id));
        menu.setHandle(request.handle());
        menu.replaceItems(buildItems(menu, language, request.items()));
        return toView(menuRepository.saveAndFlush(menu), contentService.find(store, id), language, store);
    }

    @Transactional(readOnly = true)
    public MenuView find(StoreMerchantId store, LanguageCode language, String handle)
            throws ContentNotFoundException {
        ContentMenu menu = menuRepository.findByStoreMerchantIdAndHandle(store, handle)
                .orElseThrow(() -> ContentNotFoundException.forId(-1L));
        return toView(menu, contentService.find(store, menu.getId()), language, store);
    }

    @Transactional(readOnly = true)
    public MenuView findPublished(StoreMerchantId store, LanguageCode language, String handle)
            throws ContentNotFoundException {
        MenuView menu = find(store, language, handle);
        if (menu.content().status() != ContentStatus.PUBLISHED) {
            throw ContentNotFoundException.forId(menu.content().id());
        }
        return menu;
    }

    private static void validate(MenuWriteRequest request) throws InvalidMenuTreeException {
        if (request.content().type() != ContentType.MENU) {
            throw InvalidMenuTreeException.because("content-type");
        }
        boolean tooDeep = request.items().stream().flatMap(it -> it.children().stream())
                .anyMatch(it -> !it.children().isEmpty());
        if (tooDeep) {
            throw InvalidMenuTreeException.because("maximum-depth");
        }
    }

    private static List<MenuItem> buildItems(ContentMenu menu, LanguageCode language, List<MenuItemSpec> specs) {
        List<MenuItem> result = new ArrayList<>();
        for (int position = 0; position < specs.size(); position++) {
            MenuItem root = buildItem(menu, language, specs.get(position), position);
            for (int childPosition = 0; childPosition < specs.get(position).children().size(); childPosition++) {
                root.addChild(buildItem(menu, language, specs.get(position).children().get(childPosition),
                        childPosition));
            }
            result.add(root);
        }
        return result;
    }

    private static MenuItem buildItem(ContentMenu menu, LanguageCode language, MenuItemSpec spec, int position) {
        MenuItem item = new MenuItem();
        item.setMenu(menu);
        item.setPosition(position);
        item.setTargetKind(spec.targetKind());
        item.setTargetValue(spec.targetValue());
        item.setOpenNewTab(spec.openNewTab());
        item.setVisible(spec.visible());
        item.setLoginRequired(spec.loginRequired());
        MenuItemDescription description = new MenuItemDescription();
        description.setLanguageCode(language);
        description.setLabel(spec.label());
        item.addDescription(description);
        return item;
    }

    private MenuView toView(ContentMenu menu, ContentView content, LanguageCode language, StoreMerchantId store) {
        List<MenuItem> roots = menu.getItems().stream().filter(it -> it.getParent() == null)
                .sorted(java.util.Comparator.comparingInt(MenuItem::getPosition)).toList();
        List<String> broken = new ArrayList<>();
        List<MenuItemSpec> items = roots.stream().map(it -> toSpec(it, language, store, broken)).toList();
        return new MenuView(content, menu.getHandle(), items, List.copyOf(broken));
    }

    private MenuItemSpec toSpec(MenuItem item, LanguageCode language, StoreMerchantId store, List<String> broken) {
        if (item.getTargetKind() == MenuTargetKind.CONTENT && !contentExists(store, item.getTargetValue())) {
            broken.add(item.getTargetValue());
        }
        String label = item.getDescriptions().stream().filter(it -> it.getLanguageCode().equals(language))
                .findFirst().orElseGet(() -> item.getDescriptions().getFirst()).getLabel();
        List<MenuItemSpec> children = item.getChildren().stream()
                .sorted(java.util.Comparator.comparingInt(MenuItem::getPosition))
                .map(it -> toSpec(it, language, store, broken)).toList();
        return new MenuItemSpec(label, item.getTargetKind(), item.getTargetValue(), item.isOpenNewTab(),
                item.isVisible(), item.isLoginRequired(), children);
    }

    private boolean contentExists(StoreMerchantId store, String targetValue) {
        try {
            return contentRepository.findByIdAndStoreMerchantId(Long.valueOf(targetValue), store).isPresent();
        } catch (NumberFormatException exception) {
            return false;
        }
    }
}
