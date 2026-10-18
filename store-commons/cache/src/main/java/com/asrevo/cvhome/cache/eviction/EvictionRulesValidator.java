package com.asrevo.cvhome.cache.eviction;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.EntityType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;

import com.asrevo.cvhome.commons.domain.StoreScoped;

/**
 * Checks the eviction rules against the entities Hibernate mapped, once, at start-up: an entity with a rule that
 * cannot say its store stops the service (its writes would never evict), and an entity of the package with no rule
 * is named in the log, so a table added later is not silently left out.
 */
public final class EvictionRulesValidator implements SmartInitializingSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(EvictionRulesValidator.class);

    private final EvictionRules rules;

    private final EntityManagerFactory entityManagerFactory;

    public EvictionRulesValidator(EvictionRules rules, EntityManagerFactory entityManagerFactory) {
        this.rules = rules;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (rules.entityPackage().isEmpty() || entityManagerFactory == null) {
            return;
        }
        List<String> unscoped = new ArrayList<>();
        for (EntityType<?> entity : entityManagerFactory.getMetamodel().getEntities()) {
            Class<?> type = entity.getJavaType();
            if (type == null || !rules.owns(type.getName())) {
                continue;
            }
            if (rules.regionsFor(type).isEmpty()) {
                LOG.warn("cache: entity {} has no eviction rule; a write to it leaves the cached reads stale until their ttl",
                        type.getSimpleName());
            } else if (!StoreScoped.class.isAssignableFrom(type)) {
                unscoped.add(type.getName());
            }
        }
        if (!unscoped.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "cache: entities with an eviction rule must implement StoreScoped to name their store: %s", unscoped));
        }
    }
}
