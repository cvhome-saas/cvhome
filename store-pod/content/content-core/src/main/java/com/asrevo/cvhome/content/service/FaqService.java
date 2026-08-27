package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * The starter groups, named in both platform languages.
     *
     * A store whose console and storefront are Arabic used to read "General · position 1" in an otherwise
     * Arabic screen, because the seed carried English only and the group name is shown as stored.
     */
    private static final List<String[]> DEFAULT_GROUPS = List.of(
            new String[] {"general", "General", "عام"},
            new String[] {"ordering", "Ordering", "الطلب"},
            new String[] {"shipping", "Shipping & delivery", "الشحن والتوصيل"},
            new String[] {"returns", "Returns", "الإرجاع"});

    private static final String KIND = "FAQ_GROUP";

    private final FaqGroupRepository groups;

    private final ContentRepository contents;

    @Transactional(rollbackFor = Exception.class)
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

    @Transactional(rollbackFor = Exception.class)
    public com.asrevo.cvhome.content.model.faq.FaqGroup create(StoreMerchantId store,
                                                               com.asrevo.cvhome.content.model.faq.FaqGroup body)
            throws ContentConflictException {
        if (groups.findByStoreMerchantIdAndKey(store.getId(), body.getKey()).isPresent()) {
            throw ContentConflictException.slugDuplicate(KIND, body.getKey(), store);
        }
        FaqGroup g = new FaqGroup();
        g.setStoreMerchantId(store.getId());
        apply(g, body);
        return toDto(groups.saveAndFlush(g), 0);
    }

    @Transactional(rollbackFor = Exception.class)
    public com.asrevo.cvhome.content.model.faq.FaqGroup update(StoreMerchantId store, Long id,
                                                               com.asrevo.cvhome.content.model.faq.FaqGroup body)
            throws ContentNotFoundException, ContentConflictException {
        FaqGroup g = groups.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.faqGroup(id, store));
        if (!g.getKey().equals(body.getKey())
                && groups.findByStoreMerchantIdAndKey(store.getId(), body.getKey()).isPresent()) {
            throw ContentConflictException.slugDuplicate(KIND, body.getKey(), store);
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
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(rollbackFor = Exception.class)
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
        Set<Long> requested = new HashSet<>();
        for (FaqReorder m : moves) {
            requested.add(m.getId());
        }
        renumber(all.values(), requested);
        contents.saveAll(all.values());
    }

    /**
     * Renumbers every group 0..n, keeping the order the caller asked for.
     *
     * An entry the caller actually moved wins a tie on position; id only breaks a tie between two entries
     * that were both already there. Without that, dropping an entry at position 0 of a group that already
     * had one landed it at 1 — the seller asked for the front and got second place, because the rows that
     * were there first carry lower ids. Id remains the last resort, so the ordering is still total.
     */
    private void renumber(Collection<Content> entries, Set<Long> requested) {
        Map<Long, List<Content>> perGroup = new LinkedHashMap<>();
        for (Content c : entries) {
            perGroup.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
        }
        for (List<Content> list : perGroup.values()) {
            list.sort(Comparator.comparing((Content c) -> c.getSortOrder() == null ? 0 : c.getSortOrder())
                    .thenComparing((Content c) -> requested.contains(c.getId()) ? 0 : 1)
                    .thenComparing(Content::getId));
            int i = 0;
            for (Content c : list) {
                c.setSortOrder(i++);
            }
        }
    }

    /**
     * The group id to use when a request names none: the store's first group, created on demand.
     */
    @Transactional(rollbackFor = Exception.class)
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
            g.setNames(JsonCodec.write(Map.of("en", def[1], "ar", def[2])));
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
