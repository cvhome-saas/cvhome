package com.asrevo.cvhome.content.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Ordering of the home page's sections.
 *
 * <p>
 * The whole order is sent at once and every section is renumbered {@code 0..n}, rather than accepting one move at
 * a time: a partial reorder leaves gaps and ties that the storefront would resolve arbitrarily.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SectionService {

    private final ContentRepository contents;

    @Transactional(rollbackFor = Exception.class)
    public void reorder(StoreMerchantId store, List<Long> orderedIds) throws ContentNotFoundException {
        Map<Long, Content> all = new LinkedHashMap<>();
        for (Content c : contents.findAllByType(store, ContentType.SECTION)) {
            all.put(c.getId(), c);
        }
        int position = 0;
        for (Long id : orderedIds) {
            Content c = all.remove(id);
            if (c == null) {
                throw ContentNotFoundException.byId(id, store);
            }
            c.setSortOrder(position++);
            contents.save(c);
        }
        // Anything the caller did not name keeps its relative order, after everything it did.
        for (Content c : all.values()) {
            c.setSortOrder(position++);
            contents.save(c);
        }
    }

}
