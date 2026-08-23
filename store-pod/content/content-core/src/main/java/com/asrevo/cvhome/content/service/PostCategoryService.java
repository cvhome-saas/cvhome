package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.PostCategory;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.repository.PostCategoryRepository;
import com.asrevo.cvhome.content.support.JsonCodec;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostCategoryService {

    private static final String KIND = "POST_CATEGORY";

    private final PostCategoryRepository repository;

    @Transactional(readOnly = true)
    public List<com.asrevo.cvhome.content.model.post.PostCategory> list(StoreMerchantId store) {
        List<com.asrevo.cvhome.content.model.post.PostCategory> out = new ArrayList<>();
        for (PostCategory c : repository.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId())) {
            out.add(toDto(c));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<Long, PostCategory> byIds(StoreMerchantId store) {
        Map<Long, PostCategory> out = new LinkedHashMap<>();
        for (PostCategory c : repository.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId())) {
            out.put(c.getId(), c);
        }
        return out;
    }

    @Transactional
    public com.asrevo.cvhome.content.model.post.PostCategory create(
            StoreMerchantId store, com.asrevo.cvhome.content.model.post.PostCategory body)
            throws ContentConflictException {
        if (repository.findByStoreMerchantIdAndSlug(store.getId(), body.getSlug()).isPresent()) {
            throw ContentConflictException.slugDuplicate(KIND, body.getSlug(), store);
        }
        PostCategory c = new PostCategory();
        c.setStoreMerchantId(store.getId());
        apply(c, body);
        return toDto(repository.saveAndFlush(c));
    }

    @Transactional
    public com.asrevo.cvhome.content.model.post.PostCategory update(
            StoreMerchantId store, Long id, com.asrevo.cvhome.content.model.post.PostCategory body)
            throws ContentNotFoundException, ContentConflictException {
        PostCategory c = repository.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.byId(id, store));
        if (!c.getSlug().equals(body.getSlug())
                && repository.findByStoreMerchantIdAndSlug(store.getId(), body.getSlug()).isPresent()) {
            throw ContentConflictException.slugDuplicate(KIND, body.getSlug(), store);
        }
        apply(c, body);
        return toDto(repository.saveAndFlush(c));
    }

    @Transactional
    public void delete(StoreMerchantId store, Long id) throws ContentNotFoundException {
        PostCategory c = repository.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.byId(id, store));
        repository.delete(c);
    }

    private static void apply(PostCategory c, com.asrevo.cvhome.content.model.post.PostCategory body) {
        c.setSlug(body.getSlug());
        c.setNames(JsonCodec.write(body.getNames()));
        c.setPosition(body.getPosition() != null ? body.getPosition() : 0);
    }

    @SuppressWarnings("unchecked")
    static com.asrevo.cvhome.content.model.post.PostCategory toDto(PostCategory c) {
        var d = new com.asrevo.cvhome.content.model.post.PostCategory();
        d.setId(c.getId());
        d.setSlug(c.getSlug());
        d.setNames(JsonCodec.read(c.getNames(), LinkedHashMap.class));
        d.setPosition(c.getPosition());
        return d;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> names(PostCategory c) {
        return c.getNames() == null ? Map.of() : JsonCodec.read(c.getNames(), LinkedHashMap.class);
    }

}
