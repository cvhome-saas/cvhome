package com.asrevo.cvhome.content.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

/**
 * The predicates behind every console list, shared with the summary so counts and rows agree.
 */
public final class ContentSpecifications {

    private ContentSpecifications() {
    }

    public static Specification<Content> forStoreAndType(StoreMerchantId store, ContentType type) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("storeMerchantId"), store),
                cb.equal(root.get("contentType"), type));
    }

    public static Specification<Content> withStatus(ContentStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    /**
     * Items whose {@code locale} translation is in {@code state} — or, for {@code MISSING}, items that have no row
     * for that locale at all.
     */
    public static Specification<Content> withLocaleState(LanguageCode locale, TranslationState state) {
        return (root, query, cb) -> {
            if (locale == null) {
                return cb.conjunction();
            }
            Subquery<Long> sub = query.subquery(Long.class);
            var d = sub.from(ContentDescription.class);
            List<Predicate> inner = new ArrayList<>();
            inner.add(cb.equal(d.get("content"), root));
            inner.add(cb.equal(d.get("languageCode"), locale));
            if (state != null && state != TranslationState.MISSING) {
                inner.add(cb.equal(d.get("state"), state));
            }
            sub.select(d.get("id")).where(inner.toArray(Predicate[]::new));
            return state == TranslationState.MISSING ? cb.not(cb.exists(sub)) : cb.exists(sub);
        };
    }

    /**
     * Case-insensitive contains over slug, title and body of any locale.
     */
    public static Specification<Content> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = String.format("%%%s%%", q.trim().toLowerCase(Locale.ROOT));
            Subquery<Long> sub = query.subquery(Long.class);
            var d = sub.from(ContentDescription.class);
            sub.select(d.get("id")).where(
                    cb.equal(d.get("content"), root),
                    cb.or(cb.like(cb.lower(d.get("name")), like),
                            cb.like(cb.lower(d.get("description")), like)));
            return cb.or(cb.like(cb.lower(root.get("code")), like), cb.exists(sub));
        };
    }

    /**
     * Items having at least one translation not {@code TRANSLATED} while the item itself is published or scheduled —
     * the "awaiting translation" KPI.
     */
    public static Specification<Content> awaitingTranslation() {
        return (root, query, cb) -> {
            Join<Content, ContentDescription> d = root.join("descriptions", JoinType.INNER);
            query.distinct(true);
            return cb.and(
                    root.get("status").in(ContentStatus.PUBLISHED, ContentStatus.SCHEDULED),
                    cb.notEqual(d.get("state"), TranslationState.TRANSLATED));
        };
    }

}
