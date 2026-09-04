package com.asrevo.cvhome.sso.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * The audit query, as predicates.
 *
 * <p>
 * A category filter is a set of types: the category lives on the enum rather than in a column, so the query lists the
 * types that belong to it. Text search is a contains over the three human fields — who, what and the note — because
 * that is what someone reading a log actually knows.
 * </p>
 */
public final class AuditSpecifications {

    static final String OCCURRED_AT = "occurredAt";

    private static final String EVENT_TYPE = "eventType";

    private static final String ACTOR_NAME = "actorName";

    private static final String TARGET_NAME = "targetName";

    private AuditSpecifications() {
    }

    /**
     * The types the query covers: the ones named outright, plus every type in a named category. The category lives
     * on the enum rather than in a column, so it becomes a list of types here rather than a join.
     */
    private static List<String> typesOf(AuditSearch search) {
        List<String> types = new ArrayList<>(search.types() == null ? List.of() : search.types());
        if (search.categories() != null) {
            for (AuditEventType.AuditCategory category : search.categories()) {
                for (AuditEventType type : AuditEventType.values()) {
                    if (type.category() == category) {
                        types.add(type.wire());
                    }
                }
            }
        }
        return types;
    }

    /** An id or the human name, whichever the reader had to hand. */
    private static void addIdOrName(List<Predicate> predicates, CriteriaBuilder builder, Root<AuditEventEntity> root,
                                    String idField, String nameField, String value) {
        if (StringUtils.hasText(value)) {
            predicates.add(builder.or(builder.equal(root.get(idField), value),
                    builder.equal(builder.lower(root.get(nameField)), value.toLowerCase(Locale.ROOT))));
        }
    }

    private static void addEqual(List<Predicate> predicates, CriteriaBuilder builder, Root<AuditEventEntity> root,
                                 String field, String value) {
        if (StringUtils.hasText(value)) {
            predicates.add(builder.equal(root.get(field), value));
        }
    }

    /** Free text is a contains over the three human fields: who, what, and the note. */
    private static void addText(List<Predicate> predicates, CriteriaBuilder builder, Root<AuditEventEntity> root,
                                String q) {
        if (StringUtils.hasText(q)) {
            String like = String.format("%%%s%%", q.trim().toLowerCase(Locale.ROOT));
            predicates.add(builder.or(builder.like(builder.lower(root.get(ACTOR_NAME)), like),
                    builder.like(builder.lower(root.get(TARGET_NAME)), like),
                    builder.like(builder.lower(root.get("detail")), like)));
        }
    }

    public static Specification<AuditEventEntity> of(AuditSearch search) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            List<String> types = typesOf(search);
            if (!types.isEmpty()) {
                predicates.add(root.get(EVENT_TYPE).in(types));
            }
            addIdOrName(predicates, builder, root, "actorId", ACTOR_NAME, search.actor());
            addIdOrName(predicates, builder, root, "targetId", TARGET_NAME, search.target());
            addEqual(predicates, builder, root, "clientId", search.clientId());
            addEqual(predicates, builder, root, "ip", search.ip());
            if (search.outcome() != null) {
                predicates.add(builder.equal(root.get("outcome"), search.outcome()));
            }
            addText(predicates, builder, root, search.q());
            if (search.from() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get(OCCURRED_AT), search.from()));
            }
            if (search.to() != null) {
                predicates.add(builder.lessThan(root.get(OCCURRED_AT), search.to()));
            }
            return predicates.isEmpty() ? null : builder.and(predicates.toArray(Predicate[]::new));
        };
    }

}
