package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.FaqGroup;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.model.faq.FaqReorder;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.FaqGroupRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * FAQ groups and the ordering of entries within them. An entry's group is {@code Content.parentId}, its position
 * {@code Content.sortOrder}; reordering is one atomic write of both across any number of entries.
 */
@Service
@RequiredArgsConstructor
public class FaqService {

    private static final List<String[]> DEFAULT_GROUPS = List.of(
            new String[] {"general", "General"}, new String[] {"ordering", "Ordering"},
            new String[] {"shipping", "Shipping & delivery"}, new String[] {"returns", "Returns"});

    private final FaqGroupRepository groups;

    private final ContentRepository contents;

    @Transactional
    public List<com.asrevo.cvhome.content.model.faq.FaqGroup> groups(StoreMerchantId store) {
        ensureDefaults(store);
        Map<Long, Long> counts = new LinkedHashMap<>();
        for (Content c : contents.findAllByType(store, ContentType.FAQ)) {
            if (c.getParentId() != null) {
                counts.merge(c.getParentId(), 1L, Long::sum);
            }
        }
        List<com.asrevo.cvhome.content.model.faq.FaqGroup> out = new ArrayList<>();
        for (FaqGroup g : groups.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId())) {
            out.add(toDto(g, counts.getOrDefault(g.getId(), 0L)));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public Map<Long, FaqGroup> byIds(StoreMerchantId store) {
        Map<Long, FaqGroup> out = new LinkedHashMap<>();
        for (FaqGroup g : groups.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId())) {
            out.put(g.getId(), g);
        }
        return out;
    }

    @Transactional
    public com.asrevo.cvhome.content.model.faq.FaqGroup create(StoreMerchantId store,
                                                               com.asrevo.cvhome.content.model.faq.FaqGroup body)
            throws ContentConflictException {
        if (groups.findByStoreMerchantIdAndKey(store.getId(), body.getKey()).isPresent()) {
            throw ContentConflictException.slugDuplicate("FAQ_GROUP", body.getKey(), store);
        }
        FaqGroup g = new FaqGroup();
        g.setStoreMerchantId(store.getId());
        apply(g, body);
        return toDto(groups.saveAndFlush(g), 0);
    }

    @Transactional
    public com.asrevo.cvhome.content.model.faq.FaqGroup update(StoreMerchantId store, Long id,
                                                               com.asrevo.cvhome.content.model.faq.FaqGroup body)
            throws ContentNotFoundException, ContentConflictException {
        FaqGroup g = groups.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.faqGroup(id, store));
        if (!g.getKey().equals(body.getKey())
                && groups.findByStoreMerchantIdAndKey(store.getId(), body.getKey()).isPresent()) {
            throw ContentConflictException.slugDuplicate("FAQ_GROUP", body.getKey(), store);
        }
        apply(g, body);
        FaqGroup saved = groups.saveAndFlush(g);
        long count = contents.findAllByType(store, ContentType.FAQ).stream()
                .filter(c -> saved.getId().equals(c.getParentId())).count();
        return toDto(saved, count);
    }

    /**
     * Deletes a group; its entries move to the store's first remaining group.
     */
    @Transactional
    public void delete(StoreMerchantId store, Long id) throws ContentNotFoundException {
        FaqGroup g = groups.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.faqGroup(id, store));
        groups.delete(g);
        groups.flush();
        ensureDefaults(store);
        FaqGroup fallback = groups.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId()).getFirst();
        for (Content c : contents.findAllByType(store, ContentType.FAQ)) {
            if (id.equals(c.getParentId())) {
                c.setParentId(fallback.getId());
                contents.save(c);
            }
        }
    }

    /**
     * Moves entries between groups and positions in one transaction, then renumbers every touched group 0..n.
     */
    @Transactional
    public void reorder(StoreMerchantId store, List<FaqReorder> moves) throws ContentNotFoundException {
        Map<Long, Content> all = new LinkedHashMap<>();
        for (Content c : contents.findAllByType(store, ContentType.FAQ)) {
            all.put(c.getId(), c);
        }
        Map<Long, FaqGroup> groupIds = byIds(store);
        for (FaqReorder m : moves) {
            Content c = all.get(m.getId());
            if (c == null) {
                throw ContentNotFoundException.byId(m.getId(), store);
            }
            if (!groupIds.containsKey(m.getGroupId())) {
                throw ContentNotFoundException.faqGroup(m.getGroupId(), store);
            }
            c.setParentId(m.getGroupId());
            c.setSortOrder(m.getPosition());
        }
        // renumber per group, keeping the requested order
        Map<Long, List<Content>> perGroup = new LinkedHashMap<>();
        for (Content c : all.values()) {
            perGroup.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
        }
        for (List<Content> list : perGroup.values()) {
            list.sort(Comparator.comparing((Content c) -> c.getSortOrder() == null ? 0 : c.getSortOrder())
                    .thenComparing(Content::getId));
            int i = 0;
            for (Content c : list) {
                c.setSortOrder(i++);
            }
        }
        contents.saveAll(all.values());
    }

    /**
     * The group id to use when a request names none: the store's first group, created on demand.
     */
    @Transactional
    public Long defaultGroupId(StoreMerchantId store) {
        ensureDefaults(store);
        return groups.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId()).getFirst().getId();
    }

    private void ensureDefaults(StoreMerchantId store) {
        if (!groups.findByStoreMerchantIdOrderByPositionAscIdAsc(store.getId()).isEmpty()) {
            return;
        }
        int position = 0;
        for (String[] def : DEFAULT_GROUPS) {
            FaqGroup g = new FaqGroup();
            g.setStoreMerchantId(store.getId());
            g.setKey(def[0]);
            g.setNames(JsonCodec.write(Map.of("en", def[1])));
            g.setPosition(position++);
            groups.save(g);
        }
        groups.flush();
    }

    private static void apply(FaqGroup g, com.asrevo.cvhome.content.model.faq.FaqGroup body) {
        g.setKey(body.getKey());
        g.setNames(JsonCodec.write(body.getNames()));
        g.setPosition(body.getPosition() != null ? body.getPosition() : 0);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> names(FaqGroup g) {
        return g.getNames() == null ? Map.of() : JsonCodec.read(g.getNames(), LinkedHashMap.class);
    }

    static com.asrevo.cvhome.content.model.faq.FaqGroup toDto(FaqGroup g, long count) {
        var d = new com.asrevo.cvhome.content.model.faq.FaqGroup();
        d.setId(g.getId());
        d.setKey(g.getKey());
        d.setNames(names(g));
        d.setPosition(g.getPosition());
        d.setEntryCount(count);
        return d;
    }

}
