package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.entity.ContentStatusAudit;
import com.asrevo.cvhome.content.errors.ContentErrors;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.PublishRequest;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.ContentStatusAuditRepository;
import com.asrevo.cvhome.content.support.Strings;
import com.asrevo.cvhome.errors.FieldError;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The status state machine: validates a transition, enforces the publish gate, keeps {@code visible} in step with
 * the status, writes the audit row, and runs the scheduler sweep.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublishingService {

    /**
     * A schedule this close to now is treated as "publish now".
     */
    private static final Duration SCHEDULE_GRACE = Duration.ofSeconds(30);

    private static final String FIELD = "translations.%s.%s";

    private static final String SCHEDULER = "scheduler";

    private final ContentRepository contentRepository;

    private final ContentStatusAuditRepository auditRepository;

    private final Clock clock;

    /**
     * Moves {@code entity} to {@code target}. {@code PUBLISHED} with a future {@code publishAt} becomes
     * {@code SCHEDULED}. Does not persist; the caller saves inside its own transaction.
     */
    public void transition(Content entity, ContentStatus target, PublishRequest request, LanguageCode sourceLocale,
                           ContentTypeBinding<?, ?> binding, String actor, String reason)
            throws ContentRuleException, InvalidContentRequestException {
        ContentStatus from = entity.getStatus();
        ContentStatus effective = target;
        if (target == ContentStatus.PUBLISHED || target == ContentStatus.SCHEDULED) {
            effective = schedule(entity, target, request);
            gate(entity, sourceLocale, binding);
        }
        if (from == effective) {
            return;
        }
        if (!from.canTransitionTo(effective)) {
            throw ContentRuleException.transitionNotAllowed(entity.getId(), from, effective);
        }
        apply(entity, from, effective, actor, reason);
    }

    /**
     * Applies the requested window to the entity and decides whether the result is {@code PUBLISHED} now or
     * {@code SCHEDULED} for later.
     */
    private ContentStatus schedule(Content entity, ContentStatus target, PublishRequest request)
            throws InvalidContentRequestException {
        Instant now = clock.instant();
        Instant publishAt = request != null ? request.getPublishAt() : null;
        Instant unpublishAt = request != null ? request.getUnpublishAt() : null;
        if (unpublishAt != null) {
            validateUnpublish(publishAt, unpublishAt, now);
            entity.setUnpublishAt(unpublishAt);
        }
        if (publishAt != null && publishAt.isAfter(now.plus(SCHEDULE_GRACE))) {
            entity.setPublishAt(publishAt);
            return ContentStatus.SCHEDULED;
        }
        if (target == ContentStatus.SCHEDULED) {
            throw InvalidContentRequestException.scheduleInvalid("publishAt must be in the future to schedule");
        }
        entity.setPublishAt(publishAt != null ? publishAt : now);
        return ContentStatus.PUBLISHED;
    }

    private static void validateUnpublish(Instant publishAt, Instant unpublishAt, Instant now)
            throws InvalidContentRequestException {
        if (publishAt != null && !unpublishAt.isAfter(publishAt)) {
            throw InvalidContentRequestException.scheduleInvalid("unpublishAt must be after publishAt");
        }
        if (!unpublishAt.isAfter(now)) {
            throw InvalidContentRequestException.scheduleInvalid("unpublishAt must be in the future");
        }
    }

    /**
     * Publishing needs one complete source locale: the {@code sourceLocale} if it is present, otherwise any
     * complete one. Banners additionally need alt text; types add their own rules through the binding.
     */
    public void gate(Content entity, LanguageCode sourceLocale, ContentTypeBinding<?, ?> binding)
            throws ContentRuleException {
        ContentDescription source = entity.description(sourceLocale)
                .or(() -> entity.getDescriptions().stream()
                        .filter(d -> d.getState() == TranslationState.TRANSLATED).findFirst())
                .orElseGet(entity::getDescription);
        List<FieldError> problems = source == null
                ? List.of(FieldError.of("translations", ContentErrors.PUBLISH_INCOMPLETE,
                        "At least one language must be written before publishing."))
                : sourceProblems(entity, source, binding);
        if (!problems.isEmpty()) {
            throw ContentRuleException.publishIncomplete(entity.getId(), problems);
        }
    }

    private static List<FieldError> sourceProblems(Content entity, ContentDescription source,
                                                   ContentTypeBinding<?, ?> binding) {
        List<FieldError> problems = new ArrayList<>();
        String locale = source.getLanguageCode().code();
        if (Strings.blank(source.getTitle()) && Strings.blank(source.getName())) {
            problems.add(FieldError.of(String.format(FIELD, locale, "title"), ContentErrors.PUBLISH_INCOMPLETE,
                    "Title is required."));
        }
        if (binding.requiresBody() && Strings.blank(source.getDescription())) {
            problems.add(FieldError.of(String.format(FIELD, locale, "body"), ContentErrors.PUBLISH_INCOMPLETE,
                    "Body is required."));
        }
        problems.addAll(binding.publishProblems(entity, source));
        return problems;
    }

    private void apply(Content entity, ContentStatus from, ContentStatus to, String actor, String reason) {
        entity.setStatus(to);
        entity.setVisible(to == ContentStatus.PUBLISHED);
        if (to == ContentStatus.DRAFT || to == ContentStatus.ARCHIVED) {
            entity.setPublishAt(null);
        }
        ContentStatusAudit audit = new ContentStatusAudit();
        audit.setStoreMerchantId(entity.getStoreMerchantId().getId());
        audit.setContentId(entity.getId());
        audit.setFromStatus(from);
        audit.setToStatus(to);
        audit.setActor(actor);
        audit.setReason(reason);
        audit.setOccurredAt(clock.instant());
        auditRepository.save(audit);
    }

    /**
     * Scheduler tick: promotes due {@code SCHEDULED} rows to {@code PUBLISHED} and archives {@code PUBLISHED} rows
     * past their {@code unpublishAt}. Idempotent — the predicates exclude anything already moved.
     *
     * @return the number of rows changed
     */
    @Transactional(rollbackFor = Exception.class)
    public int tick() {
        Instant now = clock.instant();
        int changed = 0;
        for (Content c : contentRepository.findDue(ContentStatus.SCHEDULED, now)) {
            apply(c, ContentStatus.SCHEDULED, ContentStatus.PUBLISHED, SCHEDULER, "publishAt reached");
            contentRepository.save(c);
            changed++;
        }
        for (Content c : contentRepository.findExpired(ContentStatus.PUBLISHED, now)) {
            apply(c, ContentStatus.PUBLISHED, ContentStatus.ARCHIVED, SCHEDULER, "unpublishAt reached");
            c.setUnpublishAt(null);
            contentRepository.save(c);
            changed++;
        }
        if (changed > 0) {
            log.info("Content scheduler moved {} item(s)", changed);
        }
        return changed;
    }

}
