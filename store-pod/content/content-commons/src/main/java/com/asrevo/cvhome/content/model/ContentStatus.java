package com.asrevo.cvhome.content.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle of a workflow content item. The legal transitions live here, in one table, so the service, the tests
 * and the console's publish menu all read the same rules.
 */
public enum ContentStatus {

    DRAFT, REVIEW, SCHEDULED, PUBLISHED, ARCHIVED;

    private static final Map<ContentStatus, Set<ContentStatus>> TRANSITIONS = Map.of(
            DRAFT, EnumSet.of(REVIEW, SCHEDULED, PUBLISHED, ARCHIVED),
            REVIEW, EnumSet.of(DRAFT, SCHEDULED, PUBLISHED, ARCHIVED),
            SCHEDULED, EnumSet.of(DRAFT, PUBLISHED, ARCHIVED),
            PUBLISHED, EnumSet.of(DRAFT, ARCHIVED, SCHEDULED),
            ARCHIVED, EnumSet.of(DRAFT));

    public boolean canTransitionTo(ContentStatus target) {
        return target != null && target != this && TRANSITIONS.get(this).contains(target);
    }

    /**
     * Whether the storefront may serve an item in this status (subject to the publish window).
     */
    public boolean live() {
        return this == PUBLISHED;
    }

}
