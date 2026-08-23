package com.asrevo.cvhome.content.model;

/**
 * Completeness of one locale of an item. {@code STALE} means the default locale changed after this one was
 * translated; it is still served but flagged for review.
 */
public enum TranslationState {
    MISSING, DRAFT, TRANSLATED, STALE
}
