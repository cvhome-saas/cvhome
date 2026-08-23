package com.asrevo.cvhome.content.facade;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.legacy.LegacyContentBox;
import com.asrevo.cvhome.content.model.legacy.LegacyContentPage;
import com.asrevo.cvhome.content.model.legacy.LegacyContentPageList;
import com.asrevo.cvhome.content.model.legacy.LegacyDescription;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * The three public reads the storefront calls today, answered in their original shapes from the extended model.
 * Deprecated from birth: it goes when landing-ui has moved to the storefront API.
 *
 * <p>
 * Semantics preserved: a page is listed when it is servable (published and inside its window; legacy rows are
 * published by the migration), {@code description} is the requested language's row (or absent), and a box is the
 * BOX row of that code regardless of workflow.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class LegacyContentFacade {

    private final ContentRepository repository;

    private final Clock clock;

    @Transactional(readOnly = true)
    public LegacyContentPageList pages(StoreMerchantId store, LanguageCode language, Pageable pageable) {
        Instant now = clock.instant();
        List<Content> all = repository.findVisibleByType(store, ContentType.PAGE).stream()
                .filter(c -> c.servable(now))
                .toList();
        int from = Math.min((int) pageable.getOffset(), all.size());
        int to = Math.min(from + pageable.getPageSize(), all.size());
        List<LegacyContentPage> items = new ArrayList<>();
        for (Content c : all.subList(from, to)) {
            items.add(page(c, language));
        }
        LegacyContentPageList list = new LegacyContentPageList();
        list.setContent(items);
        list.setTotalElements(all.size());
        list.setSize(items.size());
        list.setRecordsFiltered(items.size());
        list.setPageNumber(pageable.getPageNumber());
        list.setTotalPages(pageable.getPageSize() == 0 ? 1
                : (int) Math.ceil(all.size() / (double) pageable.getPageSize()));
        return list;
    }

    @Transactional(readOnly = true)
    public LegacyContentPage pageByName(StoreMerchantId store, LanguageCode language, String name)
            throws ContentNotFoundException {
        Instant now = clock.instant();
        Optional<Content> found = repository.findBySeUrl(store, ContentType.PAGE, name, language);
        if (found.isEmpty()) {
            found = repository.findByCodeAndType(name, ContentType.PAGE, store);
        }
        Content c = found.filter(x -> x.servable(now)).orElseThrow(() -> ContentNotFoundException.byName(name, store));
        return page(c, language);
    }

    @Transactional(readOnly = true)
    public LegacyContentPage pageByCode(StoreMerchantId store, LanguageCode language, String code)
            throws ContentNotFoundException {
        Instant now = clock.instant();
        Content c = repository.findByCodeAndType(code, ContentType.PAGE, store)
                .filter(x -> x.servable(now))
                .orElseThrow(() -> ContentNotFoundException.byCode(code, store));
        return page(c, language);
    }

    @Transactional(readOnly = true)
    public LegacyContentBox box(StoreMerchantId store, LanguageCode language, String code)
            throws ContentNotFoundException {
        Content c = repository.findByCodeAndType(code, ContentType.BOX, store)
                .orElseThrow(() -> ContentNotFoundException.byCode(code, store));
        LegacyContentBox box = new LegacyContentBox();
        box.setId(c.getId());
        box.setCode(c.getCode());
        box.setVisible(c.isVisible());
        fill(c, language, box::setDescription, box::setDescriptions);
        return box;
    }

    private LegacyContentPage page(Content c, LanguageCode language) {
        LegacyContentPage p = new LegacyContentPage();
        p.setId(c.getId());
        p.setCode(c.getCode());
        p.setVisible(c.isVisible());
        p.setLinkToMenu(c.isLinkToMenu());
        p.setPath(String.format("/content/%s", c.getCode()));
        fill(c, language, p::setDescription, p::setDescriptions);
        return p;
    }

    private void fill(Content c, LanguageCode language, java.util.function.Consumer<LegacyDescription> one,
                      java.util.function.Consumer<List<LegacyDescription>> all) {
        if (LanguageCode.isAllLanguage(language) || LanguageCode.isNonLanguage(language)) {
            all.accept(c.getDescriptions().stream().map(LegacyContentFacade::description).toList());
        }
        if (LanguageCode.isLanguage(language)) {
            c.description(language).map(LegacyContentFacade::description).ifPresent(one);
        }
    }

    static LegacyDescription description(ContentDescription d) {
        LegacyDescription out = new LegacyDescription();
        out.setId(d.getId());
        out.setLanguage(d.getLanguageCode());
        out.setName(d.getName());
        out.setDescription(d.getDescription());
        out.setFriendlyUrl(d.getSeUrl());
        out.setTitle(d.getTitle());
        out.setMetaDescription(d.getMetatagDescription());
        out.setKeyWords(d.getMetatagKeywords());
        return out;
    }

}
