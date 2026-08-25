package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.snippet.Snippet;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * The legacy {@code BOX} rows as "snippets": store-level fragments read by code ({@code meta-title},
 * {@code meta-description}, {@code header-message}, {@code agreement}, {@code LANDING_PAGE}). No workflow —
 * a {@code PUT} upserts by code.
 */
@Service
@RequiredArgsConstructor
public class SnippetService {

    private final ContentRepository repository;

    @Transactional(readOnly = true)
    public List<Snippet> list(StoreMerchantId store) {
        List<Snippet> out = new ArrayList<>();
        for (Content c : repository.findAllByType(store, ContentType.BOX)) {
            out.add(toSnippet(c));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Snippet get(StoreMerchantId store, String code) throws ContentNotFoundException {
        return toSnippet(repository.findByCodeAndType(code, ContentType.BOX, store)
                .orElseThrow(() -> ContentNotFoundException.byCode(code, store)));
    }

    @Transactional(rollbackFor = Exception.class)
    public Snippet put(StoreMerchantId store, String code, Snippet snippet, String actor) {
        Content c = repository.findByCodeAndType(code, ContentType.BOX, store).orElseGet(() -> {
            Content n = new Content();
            n.setStoreMerchantId(store);
            n.setContentType(ContentType.BOX);
            n.setCode(code);
            n.setCreatedBy(actor);
            return n;
        });
        c.setVisible(snippet.isVisible());
        // a snippet is always "live"; status mirrors visibility so the list predicates stay uniform
        c.setStatus(snippet.isVisible() ? ContentStatus.PUBLISHED : ContentStatus.DRAFT);
        c.setUpdatedBy(actor);
        ContentMapper.applyTranslations(c, snippet.getTranslations(), null, false);
        c = repository.saveAndFlush(c);
        return toSnippet(c);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(StoreMerchantId store, String code) throws ContentNotFoundException {
        Content c = repository.findByCodeAndType(code, ContentType.BOX, store)
                .orElseThrow(() -> ContentNotFoundException.byCode(code, store));
        repository.delete(c);
    }

    static Snippet toSnippet(Content c) {
        Snippet s = new Snippet();
        s.setId(c.getId());
        s.setCode(c.getCode());
        s.setVisible(c.isVisible());
        s.setTranslations(ContentMapper.translations(c));
        return s;
    }

}
