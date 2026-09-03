package com.asrevo.cvhome.sso.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Turns two snapshots into the fields that differ.
 *
 * <p>
 * Callers pass DTOs, never entities: an entity carries the password hash and lazy proxies, a DTO carries what the
 * screen shows, and the audit row is a screen. Keys that look like secrets are dropped whatever the caller passed.
 * </p>
 */
@Component
public class AuditDiff {

    static final Set<String> REDACTED = Set.of("password", "passwordHash", "clientSecret", "secret", "version");

    private final ObjectMapper json;

    public AuditDiff(ObjectMapper json) {
        this.json = json;
    }

    /** {@code [before, after]} maps holding only the top-level fields whose values differ. */
    public Diff of(Object before, Object after) {
        if (before == null || after == null) {
            // A creation or a deletion: the whole snapshot on the side that exists, nothing on the other.
            return new Diff(before == null ? null : flatten(before), after == null ? null : flatten(after));
        }
        Map<String, Object> from = flatten(before);
        Map<String, Object> to = flatten(after);
        Map<String, Object> changedFrom = new LinkedHashMap<>();
        Map<String, Object> changedTo = new LinkedHashMap<>();
        for (String key : union(from, to)) {
            Object a = from.get(key);
            Object b = to.get(key);
            if (a == null ? b != null : !a.equals(b)) {
                changedFrom.put(key, a);
                changedTo.put(key, b);
            }
        }
        return new Diff(changedFrom.isEmpty() ? null : changedFrom, changedTo.isEmpty() ? null : changedTo);
    }

    private Map<String, Object> flatten(Object value) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (value == null) {
            return out;
        }
        JsonNode node = json.valueToTree(value);
        node.properties().forEach(entry -> {
            if (!REDACTED.contains(entry.getKey())) {
                out.put(entry.getKey(), json.convertValue(entry.getValue(), Object.class));
            }
        });
        return out;
    }

    private static Set<String> union(Map<String, Object> a, Map<String, Object> b) {
        Set<String> keys = new java.util.LinkedHashSet<>(a.keySet());
        keys.addAll(b.keySet());
        return keys;
    }

    public record Diff(Map<String, Object> before, Map<String, Object> after) {
    }

}
