package com.asrevo.cvhome.cache.eviction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.event.CacheEvent;

/**
 * Which regions a committed write to an entity drops: the service's one {@code EvictionRules} bean.
 *
 * <p>
 * Declared once, next to the regions, so what a write invalidates can be read in one place. Only entities under
 * {@link #entityPackage()} are watched at all (the outbox's rows are written every few seconds and change nothing a
 * shopper sees); an entity of the package with no rule is left alone and named once at start-up, never taken as a
 * reason to clear every store.
 * </p>
 *
 * <p>
 * The same builder maps a {@link CacheEvent} to regions ({@code .onEvent(StockChanged.class).evict(SKU)}): the
 * service's own events, applied again when the outbox drains them, and the foreign events a transport brings in.
 * An event with no rule drops nothing.
 * </p>
 */
public final class EvictionRules {

    private static final String NO_REGION = "a rule names at least one region";

    private final String entityPackage;

    private final Map<Class<?>, Set<CacheRegion>> byEntity;

    private final Map<Class<? extends CacheEvent>, Set<CacheRegion>> byEvent;

    private EvictionRules(String entityPackage, Map<Class<?>, Set<CacheRegion>> byEntity,
                          Map<Class<? extends CacheEvent>, Set<CacheRegion>> byEvent) {
        this.entityPackage = entityPackage;
        this.byEntity = byEntity;
        this.byEvent = byEvent;
    }

    /** Rules for the entities under {@code entityPackage}. */
    public static Builder in(String entityPackage) {
        if (entityPackage == null || entityPackage.isBlank()) {
            throw new IllegalArgumentException("eviction rules watch one entity package");
        }
        return new Builder(entityPackage);
    }

    /** A service that caches nothing of its own tables. */
    public static EvictionRules none() {
        return new EvictionRules("", Map.of(), Map.of());
    }

    /** Rules for a service that caches nothing of its own tables but maps the events a transport brings in. */
    public static Builder onEvents() {
        return new Builder("");
    }

    public String entityPackage() {
        return entityPackage;
    }

    /** Whether a write to an entity of this class name is watched: it is under the package. */
    public boolean owns(String entityClassName) {
        return !entityPackage.isEmpty() && entityClassName != null && entityClassName.startsWith(entityPackage);
    }

    /** The regions a write to {@code entity} drops; empty when no rule names it. */
    public Set<CacheRegion> regionsFor(Class<?> entity) {
        Set<CacheRegion> regions = byEntity.get(entity);
        return regions == null ? Set.of() : regions;
    }

    public Set<Class<?>> entities() {
        return byEntity.keySet();
    }

    /** The regions a received {@code event} drops; empty when no rule names it. */
    public Set<CacheRegion> regionsForEvent(Class<? extends CacheEvent> event) {
        Set<CacheRegion> regions = byEvent.get(event);
        return regions == null ? Set.of() : regions;
    }

    public Set<Class<? extends CacheEvent>> events() {
        return byEvent.keySet();
    }

    /** Builds the rules: {@code .on(entities).evict(regions)} as often as needed. */
    public static final class Builder {

        private final String entityPackage;

        private final Map<Class<?>, Set<CacheRegion>> byEntity = new LinkedHashMap<>();

        private final Map<Class<? extends CacheEvent>, Set<CacheRegion>> byEvent = new LinkedHashMap<>();

        private Builder(String entityPackage) {
            this.entityPackage = entityPackage;
        }

        public On on(Class<?>... entities) {
            return on(List.of(entities));
        }

        public On on(Collection<Class<?>> entities) {
            if (entityPackage.isEmpty()) {
                throw new IllegalArgumentException("entity rules need the entity package: EvictionRules.in(...)");
            }
            if (entities.isEmpty()) {
                throw new IllegalArgumentException("a rule names at least one entity");
            }
            for (Class<?> entity : entities) {
                if (!entity.getName().startsWith(entityPackage)) {
                    throw new IllegalArgumentException(String.format("%s is not under %s", entity.getName(),
                            entityPackage));
                }
            }
            return new On(this, new ArrayList<>(entities));
        }

        @SafeVarargs
        public final OnEvent onEvent(Class<? extends CacheEvent>... events) {
            if (events.length == 0) {
                throw new IllegalArgumentException("a rule names at least one event");
            }
            return new OnEvent(this, List.of(events));
        }

        public EvictionRules build() {
            Map<Class<?>, Set<CacheRegion>> frozen = new LinkedHashMap<>();
            byEntity.forEach((entity, regions) -> frozen.put(entity, Collections.unmodifiableSet(regions)));
            Map<Class<? extends CacheEvent>, Set<CacheRegion>> frozenEvents = new LinkedHashMap<>();
            byEvent.forEach((event, regions) -> frozenEvents.put(event, Collections.unmodifiableSet(regions)));
            return new EvictionRules(entityPackage, Collections.unmodifiableMap(frozen),
                    Collections.unmodifiableMap(frozenEvents));
        }

        /** The entities of one rule, waiting for their regions. */
        public static final class On {

            private final Builder builder;

            private final List<Class<?>> entities;

            private On(Builder builder, List<Class<?>> entities) {
                this.builder = builder;
                this.entities = entities;
            }

            public Builder evict(CacheRegion... regions) {
                return evict(List.of(regions));
            }

            public Builder evict(Collection<? extends CacheRegion> regions) {
                if (regions.isEmpty()) {
                    throw new IllegalArgumentException(NO_REGION);
                }
                for (Class<?> entity : entities) {
                    builder.byEntity.computeIfAbsent(entity, ignored -> new LinkedHashSet<>()).addAll(regions);
                }
                return builder;
            }
        }

        /** The events of one rule, waiting for their regions. */
        public static final class OnEvent {

            private final Builder builder;

            private final List<Class<? extends CacheEvent>> events;

            private OnEvent(Builder builder, List<Class<? extends CacheEvent>> events) {
                this.builder = builder;
                this.events = events;
            }

            public Builder evict(CacheRegion... regions) {
                return evict(List.of(regions));
            }

            public Builder evict(Collection<? extends CacheRegion> regions) {
                if (regions.isEmpty()) {
                    throw new IllegalArgumentException(NO_REGION);
                }
                for (Class<? extends CacheEvent> event : events) {
                    builder.byEvent.computeIfAbsent(event, ignored -> new LinkedHashSet<>()).addAll(regions);
                }
                return builder;
            }
        }
    }
}
