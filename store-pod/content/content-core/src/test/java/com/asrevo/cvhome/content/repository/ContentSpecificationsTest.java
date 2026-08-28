package com.asrevo.cvhome.content.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.ContentFixtures;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The predicates behind every console list. Built against a mocked criteria API — the point is which branch each
 * filter takes (a null filter must widen to a conjunction rather than narrow to nothing, and MISSING inverts the
 * locale sub-query), not the SQL the provider then emits.
 */
class ContentSpecificationsTest {

    private static final String STATUS = "status";

    private Root<Content> root;

    private CriteriaQuery<?> query;

    private CriteriaBuilder cb;

    private Predicate conjunction;

    private Subquery<Long> subquery;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        root = mock(Root.class, RETURNS_DEEP_STUBS);
        query = mock(CriteriaQuery.class);
        cb = mock(CriteriaBuilder.class, RETURNS_DEEP_STUBS);
        conjunction = mock(Predicate.class);
        subquery = mock(Subquery.class, RETURNS_DEEP_STUBS);
        when(cb.conjunction()).thenReturn(conjunction);
        when(query.subquery(Long.class)).thenReturn(subquery);
        when(subquery.from(ContentDescription.class)).thenReturn(mock(Root.class, RETURNS_DEEP_STUBS));
    }

    @Test
    void theStoreAndTypeAreAlwaysBothRequired() {
        ContentSpecifications.forStoreAndType(ContentFixtures.STORE, ContentType.PAGE)
                .toPredicate(root, query, cb);

        verify(root).get("storeMerchantId");
        verify(root).get("contentType");
        verify(cb).and(any(Predicate.class), any(Predicate.class));
    }

    @Test
    void aNullStatusWidensRatherThanNarrows() {
        assertThat(ContentSpecifications.withStatus(null).toPredicate(root, query, cb)).isSameAs(conjunction);
        verify(root, never()).get(STATUS);
    }

    @Test
    void aStatusFilterComparesTheColumn() {
        ContentSpecifications.withStatus(ContentStatus.PUBLISHED).toPredicate(root, query, cb);

        verify(root).get(STATUS);
    }

    @Test
    void aNullLocaleSkipsTheSubqueryEntirely() {
        assertThat(ContentSpecifications.withLocaleState(null, TranslationState.MISSING).toPredicate(root, query, cb))
                .isSameAs(conjunction);
        verify(query, never()).subquery(Long.class);
    }

    @Test
    void aLocaleWithoutAStateAsksOnlyThatTheRowExists() {
        ContentSpecifications.withLocaleState(ContentFixtures.EN, null).toPredicate(root, query, cb);

        verify(cb).exists(subquery);
        verify(cb, never()).not(any(Expression.class));
    }

    @Test
    void missingInvertsTheExistenceCheck() {
        ContentSpecifications.withLocaleState(ContentFixtures.EN, TranslationState.MISSING)
                .toPredicate(root, query, cb);

        verify(cb).not(any(Expression.class));
    }

    @Test
    void aConcreteStateNarrowsTheSubqueryAndKeepsItPositive() {
        ContentSpecifications.withLocaleState(ContentFixtures.EN, TranslationState.STALE)
                .toPredicate(root, query, cb);

        verify(cb).exists(subquery);
        verify(cb, never()).not(any(Expression.class));
    }

    @Test
    void blankSearchTextWidensRatherThanNarrows() {
        assertThat(ContentSpecifications.search(null).toPredicate(root, query, cb)).isSameAs(conjunction);
        assertThat(ContentSpecifications.search("   ").toPredicate(root, query, cb)).isSameAs(conjunction);
        verify(query, never()).subquery(Long.class);
    }

    @Test
    void searchLooksAtTheSlugAndAtEveryLocalesTitleAndBody() {
        ContentSpecifications.search("  Terms  ").toPredicate(root, query, cb);

        verify(root).get("code");
        verify(cb).exists(subquery);
        verify(cb, org.mockito.Mockito.atLeastOnce()).like(any(Expression.class), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void awaitingTranslationJoinsTheLocalesAndDeduplicates() {
        Join<Content, ContentDescription> join = mock(Join.class, RETURNS_DEEP_STUBS);
        when(root.join("descriptions", JoinType.INNER)).thenReturn((Join) join);

        ContentSpecifications.awaitingTranslation().toPredicate(root, query, cb);

        verify(query).distinct(true);
        verify(join).get("state");
        verify(root).get(STATUS);
    }

}
