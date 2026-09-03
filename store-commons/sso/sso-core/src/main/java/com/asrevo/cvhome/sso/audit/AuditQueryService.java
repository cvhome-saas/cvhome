package com.asrevo.cvhome.sso.audit;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.dto.AuditEventDto;
import com.asrevo.cvhome.uaa.errors.AuditEventNotFoundException;
import com.asrevo.cvhome.uaa.errors.AuditExportTooLargeException;
import com.asrevo.cvhome.uaa.errors.AuditQueryInvalidException;

import lombok.RequiredArgsConstructor;

/**
 * Reading the audit log: a page, one event, and a CSV of everything that matches.
 *
 * <p>
 * The export streams a page at a time rather than loading the match set: an audit log is the one table that grows
 * without a ceiling, and a year of it will not fit in a response buffer. The cap is checked before a byte is written,
 * because half a file that stops mid-row is worse than a refusal.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AuditQueryService {

    /** The most rows one export may hold. Beyond it, narrow the range. */
    public static final int EXPORT_CAP = 100_000;

    /** A page bigger than this is a mistake, not a request. */
    public static final int MAX_PAGE_SIZE = 200;

    static final int EXPORT_PAGE = 1_000;

    private static final List<String> HEADER = List.of("occurredAt", "eventType", "outcome", "reasonCode", "actorType",
            "actorId", "actorName", "targetType", "targetId", "targetName", "clientId", "ip", "detail", "traceId");

    private final AuditEventRepository repository;

    @Transactional(readOnly = true)
    public Page<AuditEventDto> search(AuditSearch search, Pageable pageable) throws AuditQueryInvalidException {
        validate(search);
        return repository.findAll(AuditSpecifications.of(search), newest(pageable)).map(AuditQueryService::toDto);
    }

    @Transactional(readOnly = true)
    public AuditEventDto findOne(long id) throws AuditEventNotFoundException {
        return repository.findById(id).map(AuditQueryService::toDto)
                .orElseThrow(() -> AuditEventNotFoundException.of(id));
    }

    /** Writes the whole match set as CSV, oldest first — the order a reader follows a story in. */
    @Transactional(readOnly = true)
    public long exportCsv(AuditSearch search, Writer out) throws AuditQueryInvalidException, AuditExportTooLargeException,
            IOException {
        validate(search);
        var specification = AuditSpecifications.of(search);
        long matched = repository.count(specification);
        if (matched > EXPORT_CAP) {
            throw AuditExportTooLargeException.of(matched, EXPORT_CAP);
        }
        CsvWriter csv = new CsvWriter(out);
        csv.row(HEADER);
        long written = 0;
        for (int page = 0; page * (long) EXPORT_PAGE < matched; page++) {
            Pageable slice = PageRequest.of(page, EXPORT_PAGE, Sort.by(AuditSpecifications.OCCURRED_AT).ascending());
            for (AuditEventEntity event : repository.findAll(specification, slice)) {
                csv.row(toRow(event));
                written++;
            }
            out.flush();
        }
        return written;
    }

    private static void validate(AuditSearch search) throws AuditQueryInvalidException {
        Instant from = search.from();
        Instant to = search.to();
        if (from != null && to != null && !from.isBefore(to)) {
            throw AuditQueryInvalidException.of("from", "The start of the range must be before its end.");
        }
    }

    /** Newest first unless the caller asked otherwise: an audit log is read from the top. */
    private static Pageable newest(Pageable pageable) {
        return pageable.getSort().isSorted() ? pageable
                : PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), MAX_PAGE_SIZE),
                        Sort.by(AuditSpecifications.OCCURRED_AT).descending());
    }

    private static List<String> toRow(AuditEventEntity e) {
        return List.of(String.valueOf(e.getOccurredAt()), e.getEventType(), String.valueOf(e.getOutcome()),
                nullToEmpty(e.getReasonCode()), String.valueOf(e.getActorType()), nullToEmpty(e.getActorId()),
                nullToEmpty(e.getActorName()), String.valueOf(e.getTargetType()), nullToEmpty(e.getTargetId()),
                nullToEmpty(e.getTargetName()), nullToEmpty(e.getClientId()), nullToEmpty(e.getIp()),
                nullToEmpty(e.getDetail()), nullToEmpty(e.getTraceId()));
    }

    static AuditEventDto toDto(AuditEventEntity e) {
        AuditEventType.AuditCategory category;
        try {
            category = AuditEventType.fromWire(e.getEventType()).category();
        } catch (IllegalArgumentException ex) {
            // A row written by an older version, whose type this build no longer knows. It still reads.
            category = null;
        }
        return new AuditEventDto(e.getId(), e.getOccurredAt(), e.getEventType(), category, e.getOutcome(),
                e.getReasonCode(), e.getActorType(), e.getActorId(), e.getActorName(), e.getTargetType(), e.getTargetId(),
                e.getTargetName(), e.getClientId(), e.getIp(), e.getUserAgent(), e.getBefore(), e.getAfter(), e.getDetail(),
                e.getTraceId());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
