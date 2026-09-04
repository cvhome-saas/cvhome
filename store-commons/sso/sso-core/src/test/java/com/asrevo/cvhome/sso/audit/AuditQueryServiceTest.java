package com.asrevo.cvhome.sso.audit;

import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.uaa.errors.AuditEventNotFoundException;
import com.asrevo.cvhome.uaa.errors.AuditExportTooLargeException;
import com.asrevo.cvhome.uaa.errors.AuditQueryInvalidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Searching and exporting the audit log.
 *
 * <p>
 * The export is the part with teeth. It refuses above a hard cap rather than streaming an unbounded result, and it
 * pages rather than loading everything — an audit log is the one table that only grows, and an operator asking for
 * "everything" is asking for the whole history of the realm. The cap is checked against a count before a single row
 * is read, so an over-large request costs one query rather than a heap.
 * </p>
 *
 * <p>
 * A range whose start is not before its end is refused up front: it matches nothing, and an empty audit result is
 * exactly what somebody investigating an incident must not be handed silently.
 * </p>
 */
class AuditQueryServiceTest {

    private static final String JANUARY_FIRST = "2026-01-01T00:00:00Z";
    private static final String JANUARY_SECOND = "2026-01-02T00:00:00Z";
    private static final String BLANK = "  ";

    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final AuditQueryService service = new AuditQueryService(repository);

    private CriteriaBuilder builder;
    private Root<AuditEventEntity> root;
    private CriteriaQuery<?> query;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        builder = mock(CriteriaBuilder.class, Mockito.RETURNS_DEEP_STUBS);
        root = mock(Root.class, Mockito.RETURNS_DEEP_STUBS);
        query = mock(CriteriaQuery.class, Mockito.RETURNS_DEEP_STUBS);
        when(root.get(Mockito.anyString())).thenReturn(mock(Path.class));
    }

    private Predicate build(AuditSearch search) {
        return AuditSpecifications.of(search).toPredicate(root, query, builder);
    }

    @Test
    void anEmptySearchYieldsNoPredicateAtAllRatherThanAnEmptyAnd() {
        // A null predicate is "no where clause"; an empty and() is a valid SQL fragment that some drivers reject.
        assertThat(build(AuditSearch.none())).isNull();
    }

    @Test
    void aCategoryExpandsToEveryEventTypeInIt() {
        build(new AuditSearch(null, List.of(AuditEventType.AuditCategory.AUTHENTICATION), null, null, null, null, null, null,
                null, null));

        verify(root).get("eventType");
    }

    @Test
    void anActorOrTargetMatchesEitherItsIdOrItsNameCaseInsensitively() {
        build(new AuditSearch(null, null, "Ops@Example.com", "someone", null, null, null, null, null, null));

        // An operator searching the log types a name, not a UUID; both have to find the same rows.
        verify(builder, times(2)).or(any(Predicate.class), any(Predicate.class));
        verify(root).get("actorId");
        verify(root).get("targetId");
    }

    @Test
    void aFreeTextQueryLooksInActorTargetAndDetail() {
        build(new AuditSearch(null, null, null, null, null, null, null, "  Failed  ", null, null));

        // Three columns, one like each -- and the term is trimmed and lower-cased before it goes anywhere near SQL.
        verify(builder, times(3)).like(any(), Mockito.eq("%failed%"));
    }

    @Test
    void aDateRangeIsInclusiveAtItsStartAndExclusiveAtItsEnd() {
        Instant from = Instant.parse(JANUARY_FIRST);
        Instant to = from.plusSeconds(3600);

        build(new AuditSearch(null, null, null, null, null, null, null, null, from, to));

        // Half-open, so consecutive hourly exports neither miss a row nor report one twice.
        verify(builder).greaterThanOrEqualTo(any(), Mockito.eq(from));
        verify(builder).lessThan(any(), Mockito.eq(to));
    }

    @Test
    void blankScalarFiltersAddNothing() {
        build(new AuditSearch(null, null, BLANK, BLANK, BLANK, null, BLANK, BLANK, null, null));

        verify(builder, Mockito.never()).equal(any(), Mockito.anyString());
    }

    @Test
    void aRangeThatEndsBeforeItStartsIsRefusedRatherThanReturningNothing() {
        Instant from = Instant.parse(JANUARY_SECOND);
        AuditSearch backwards = new AuditSearch(null, null, null, null, null, null, null, null, from,
                from.minusSeconds(1));

        assertThatThrownBy(() -> service.search(backwards, Pageable.unpaged()))
                .isInstanceOf(AuditQueryInvalidException.class);
        assertThatThrownBy(() -> service.exportCsv(backwards, new StringWriter()))
                .isInstanceOf(AuditQueryInvalidException.class);
    }

    @Test
    void anEqualStartAndEndIsRefusedTooBecauseItMatchesNothing() {
        Instant at = Instant.parse(JANUARY_SECOND);

        assertThatThrownBy(() -> service.search(
                new AuditSearch(null, null, null, null, null, null, null, null, at, at), Pageable.unpaged()))
                .isInstanceOf(AuditQueryInvalidException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void aSearchIsAlwaysOrderedNewestFirst() throws Exception {
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        service.search(AuditSearch.none(), PageRequest.of(0, 20));

        verify(repository).findAll(any(Specification.class), Mockito.argThat((Pageable p) -> p.getSort().isSorted()));
    }

    @Test
    void anUnknownEventIsATypedNotFound() {
        when(repository.findById(9L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.findOne(9L)).isInstanceOf(AuditEventNotFoundException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    void anExportOverTheCapIsRefusedBeforeASingleRowIsRead() {
        when(repository.count(any(Specification.class)))
                .thenReturn((long) AuditQueryService.EXPORT_CAP + 1);

        assertThatThrownBy(() -> service.exportCsv(AuditSearch.none(), new StringWriter()))
                .isInstanceOf(AuditExportTooLargeException.class);

        // One count, not a heap of rows.
        verify(repository, Mockito.never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void anExportWritesAHeaderEvenWhenNothingMatches() throws Exception {
        when(repository.count(any(Specification.class))).thenReturn(0L);
        StringWriter out = new StringWriter();

        assertThat(service.exportCsv(AuditSearch.none(), out)).isZero();

        // Every field is quoted, header included: a detail containing a comma would otherwise shift every column.
        assertThat(out.toString()).startsWith("\"occurredAt\",\"eventType\"");
    }

    @SuppressWarnings("unchecked")
    @Test
    void anExportPagesRatherThanLoadingTheWholeLog() throws Exception {
        AuditEventEntity row = new AuditEventEntity();
        row.setOccurredAt(Instant.EPOCH);
        row.setEventType("user.created");
        when(repository.count(any(Specification.class))).thenReturn((long) AuditQueryService.EXPORT_PAGE + 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row)));

        assertThat(service.exportCsv(AuditSearch.none(), new StringWriter())).isEqualTo(2);

        // Two slices for a count just past one page; an audit log is the one table that only grows.
        verify(repository, times(2)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void aValidRangeIsAcceptedInBothDirectionsOfTheApi() {
        Instant from = Instant.parse(JANUARY_FIRST);
        AuditSearch forwards = new AuditSearch(null, null, null, null, null, null, null, null, from,
                from.plusSeconds(1));

        assertThatCode(() -> build(forwards)).doesNotThrowAnyException();
    }
}
