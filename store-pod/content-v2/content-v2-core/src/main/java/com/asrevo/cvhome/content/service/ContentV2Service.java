package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.content.Content;
import com.asrevo.cvhome.content.entity.content.ContentAudit;
import com.asrevo.cvhome.content.entity.content.ContentDescription;
import com.asrevo.cvhome.content.entity.content.ContentRedirect;
import com.asrevo.cvhome.content.entity.content.ContentRevision;
import com.asrevo.cvhome.content.entity.content.ContentStatusAudit;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentVersionConflictException;
import com.asrevo.cvhome.content.errors.IllegalContentTransitionException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentSummary;
import com.asrevo.cvhome.content.model.ContentType;
import com.asrevo.cvhome.content.model.ContentView;
import com.asrevo.cvhome.content.model.ContentWriteRequest;
import com.asrevo.cvhome.content.model.LifecycleRequest;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.repository.ContentAuditRepository;
import com.asrevo.cvhome.content.repository.ContentRedirectRepository;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.ContentRevisionRepository;
import com.asrevo.cvhome.content.repository.ContentStatusAuditRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class ContentV2Service {
    private static final String SCHEDULER_ACTOR = "content-scheduler";
    private final ContentRepository contentRepository;
    private final ContentRevisionRepository revisionRepository;
    private final ContentAuditRepository auditRepository;
    private final ContentStatusAuditRepository statusAuditRepository;
    private final ContentRedirectRepository redirectRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ContentV2Service(ContentRepository contentRepository, ContentRevisionRepository revisionRepository,
                            ContentAuditRepository auditRepository,
                            ContentStatusAuditRepository statusAuditRepository,
                            ContentRedirectRepository redirectRepository, ObjectMapper objectMapper) {
        this.contentRepository = contentRepository;
        this.revisionRepository = revisionRepository;
        this.auditRepository = auditRepository;
        this.statusAuditRepository = statusAuditRepository;
        this.redirectRepository = redirectRepository;
        this.objectMapper = objectMapper;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public ContentView create(StoreMerchantId store, LanguageCode language, ContentWriteRequest request,
                              String actor) {
        Content content = new Content();
        content.setStoreMerchantId(store);
        content.setCode(request.code());
        content.setContentType(request.type());
        content.getAuditSection().setDateCreated(clock.instant());
        content.getAuditSection().setDateModified(clock.instant());
        applyTranslation(content, store, language, request);
        Content saved = contentRepository.saveAndFlush(content);
        saveRevision(saved, actor);
        saveAudit(saved, "CREATE", actor, null, toView(saved));
        return toView(saved);
    }

    @Transactional(readOnly = true)
    public ContentView find(StoreMerchantId store, Long id) throws ContentNotFoundException {
        return toView(require(store, id));
    }

    @Transactional(readOnly = true)
    public List<ContentView> list(StoreMerchantId store) {
        return contentRepository.findAllByStoreMerchantIdOrderByAuditSectionDateModifiedDesc(store).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentView findPublishedRoute(StoreMerchantId store, LanguageCode language, ContentType type,
                                          String slug) throws ContentNotFoundException {
        Content content = contentRepository.findRoute(store, language, type, ContentStatus.PUBLISHED, slug)
                .filter(this::isEffective)
                .orElseThrow(() -> ContentNotFoundException.forId(-1L));
        return toView(content);
    }

    @Transactional(readOnly = true)
    public ResolvedRoute findPublishedRouteWithFallback(StoreMerchantId store, LanguageCode language,
                                                         ContentType type, String slug)
            throws ContentNotFoundException {
        try {
            return new ResolvedRoute(findPublishedRoute(store, language, type, slug), language, false);
        } catch (ContentNotFoundException exception) {
            LanguageCode fallback = LanguageCode.defaultLanguage();
            if (fallback.equals(language)) {
                throw exception;
            }
            return new ResolvedRoute(findPublishedRoute(store, fallback, type, slug), fallback, true);
        }
    }

    @Transactional(readOnly = true)
    public List<ContentView> searchPublished(StoreMerchantId store, LanguageCode language, String query, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return contentRepository.searchPublished(store, language, ContentStatus.PUBLISHED, query,
                        PageRequest.of(0, safeLimit)).stream()
                .filter(this::isEffective).map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<ContentView> listPublished(StoreMerchantId store) {
        return contentRepository.findAllByStoreMerchantIdAndStatusOrderByContentTypeAscIdAsc(
                        store, ContentStatus.PUBLISHED).stream()
                .filter(this::isEffective).map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public ContentSummary publishedSummary(StoreMerchantId store) {
        List<ContentView> published = listPublished(store);
        Map<ContentType, Long> byType = published.stream().collect(Collectors.groupingBy(
                ContentView::type, () -> new java.util.EnumMap<>(ContentType.class), Collectors.counting()));
        return new ContentSummary(published.size(), Map.copyOf(byType));
    }

    @Transactional
    public ContentView update(StoreMerchantId store, LanguageCode language, Long id, long expectedVersion,
                              ContentWriteRequest request, String actor) throws ContentNotFoundException,
            ContentVersionConflictException {
        Content content = require(store, id);
        verifyVersion(content, expectedVersion);
        ContentView before = toView(content);
        ContentDescription description = findTranslation(content, language);
        String oldSlug = description == null ? null : description.getSeUrl();
        boolean publishedSlugChanged = content.getStatus() == ContentStatus.PUBLISHED
                && oldSlug != null && !oldSlug.equals(request.slug());
        content.setCode(request.code());
        content.getAuditSection().setDateModified(clock.instant());
        applyTranslation(content, store, language, request);
        content.getDescriptions().stream()
                .filter(it -> !it.getLanguageCode().equals(language))
                .forEach(it -> it.setTranslationState(TranslationState.STALE));
        Content saved = contentRepository.saveAndFlush(content);
        if (publishedSlugChanged) {
            saveRedirect(saved, store, language, oldSlug);
        }
        saveRevision(saved, actor);
        saveAudit(saved, "UPDATE", actor, before, toView(saved));
        return toView(saved);
    }

    @Transactional
    public ContentView transition(StoreMerchantId store, Long id, long expectedVersion, ContentStatus target,
                                  LifecycleRequest request, String actor) throws ContentNotFoundException,
            ContentVersionConflictException, IllegalContentTransitionException {
        Content content = require(store, id);
        verifyVersion(content, expectedVersion);
        ContentView before = toView(content);
        ContentStatus previous = content.getStatus();
        content.transition(target, request.publishAt(), request.unpublishAt(), actor, clock.instant());
        Content saved = contentRepository.saveAndFlush(content);
        saveRevision(saved, actor);
        saveStatusAudit(saved, previous, target, actor, request.reason());
        saveAudit(saved, "STATUS_CHANGE", actor, before, toView(saved));
        return toView(saved);
    }

    @Transactional
    public int processDueContent() throws ContentNotFoundException, ContentVersionConflictException,
            IllegalContentTransitionException {
        Instant now = clock.instant();
        int processed = 0;
        for (Content content : contentRepository.findAllByStatusAndPublishAtLessThanEqual(
                ContentStatus.SCHEDULED, now)) {
            transition(content.getStoreMerchantId(), content.getId(), content.getVersion(), ContentStatus.PUBLISHED,
                    new LifecycleRequest(null, content.getUnpublishAt(), "scheduled-publish"), SCHEDULER_ACTOR);
            processed++;
        }
        for (Content content : contentRepository.findAllByStatusAndUnpublishAtLessThanEqual(
                ContentStatus.PUBLISHED, now)) {
            transition(content.getStoreMerchantId(), content.getId(), content.getVersion(),
                    ContentStatus.UNPUBLISHED, new LifecycleRequest(null, null, "scheduled-unpublish"),
                    SCHEDULER_ACTOR);
            processed++;
        }
        return processed;
    }

    private Content require(StoreMerchantId store, Long id) throws ContentNotFoundException {
        return contentRepository.findByIdAndStoreMerchantId(id, store)
                .orElseThrow(() -> ContentNotFoundException.forId(id));
    }

    private boolean isEffective(Content content) {
        Instant now = clock.instant();
        boolean started = content.getPublishAt() == null || !content.getPublishAt().isAfter(now);
        boolean notEnded = content.getUnpublishAt() == null || content.getUnpublishAt().isAfter(now);
        return started && notEnded && content.getDeletedAt() == null;
    }

    private static void verifyVersion(Content content, long expected) throws ContentVersionConflictException {
        if (content.getVersion() != expected) {
            throw ContentVersionConflictException.expected(expected, content.getVersion());
        }
    }

    private static void applyTranslation(Content content, StoreMerchantId store, LanguageCode language,
                                         ContentWriteRequest request) {
        ContentDescription description = content.getDescriptions().stream()
                .filter(it -> it.getLanguageCode().equals(language))
                .findFirst()
                .orElseGet(() -> {
                    ContentDescription created = new ContentDescription();
                    created.setLanguageCode(language);
                    created.setStoreMerchantId(store);
                    created.setContentType(request.type());
                    content.addDescription(created);
                    return created;
                });
        description.setName(request.name());
        description.setTitle(request.title());
        description.setDescription(request.description());
        description.setSeUrl(request.slug());
        description.setMetatagTitle(request.metaTitle());
        description.setMetatagDescription(request.metaDescription());
        description.setMetatagKeywords(request.metaKeywords());
        description.setCanonicalUrl(request.canonicalUrl());
        description.setNoIndex(request.noIndex());
        description.setTranslationState(TranslationState.CURRENT);
    }

    private static ContentDescription findTranslation(Content content, LanguageCode language) {
        return content.getDescriptions().stream().filter(it -> it.getLanguageCode().equals(language))
                .findFirst().orElse(null);
    }

    private void saveRedirect(Content content, StoreMerchantId store, LanguageCode language, String oldSlug) {
        ContentRedirect redirect = redirectRepository.findByStoreMerchantIdAndLanguageCodeAndOldPath(
                store, language, oldSlug).orElseGet(ContentRedirect::new);
        redirect.setStoreMerchantId(store);
        redirect.setLanguageCode(language);
        redirect.setOldPath(oldSlug);
        redirect.setDestinationContentId(content.getId());
        redirectRepository.save(redirect);
    }

    private void saveStatusAudit(Content content, ContentStatus previous, ContentStatus target, String actor,
                                 String reason) {
        ContentStatusAudit audit = new ContentStatusAudit();
        audit.setContentId(content.getId());
        audit.setStoreMerchantId(content.getStoreMerchantId());
        audit.setFromStatus(previous);
        audit.setToStatus(target);
        audit.setActor(actor);
        audit.setReason(reason);
        audit.setOccurredAt(clock.instant());
        statusAuditRepository.save(audit);
    }

    private void saveAudit(Content content, String action, String actor, ContentView before, ContentView after) {
        ContentAudit audit = new ContentAudit();
        audit.setContentId(content.getId());
        audit.setStoreMerchantId(content.getStoreMerchantId());
        audit.setAction(action);
        audit.setActor(actor);
        audit.setBeforeSummary(before == null ? null : objectMapper.writeValueAsString(before));
        audit.setAfterSummary(objectMapper.writeValueAsString(after));
        audit.setOccurredAt(clock.instant());
        auditRepository.save(audit);
    }

    private void saveRevision(Content content, String actor) {
        ContentRevision revision = new ContentRevision();
        revision.setContent(content);
        revision.setVersion(content.getVersion());
        revision.setSnapshot(objectMapper.writeValueAsString(toView(content)));
        revision.setAuthor(actor);
        revision.setCreatedAt(Instant.now(clock));
        revisionRepository.save(revision);
    }

    private ContentView toView(Content content) {
        List<ContentView.TranslationView> translations = content.getDescriptions().stream()
                .map(it -> new ContentView.TranslationView(it.getLanguageCode(), it.getTranslationState(),
                        it.getName(), it.getTitle(), it.getDescription(), it.getSeUrl(), it.getMetatagTitle(),
                        it.getMetatagDescription(), it.getMetatagKeywords(), it.getCanonicalUrl(), it.isNoIndex()))
                .toList();
        return new ContentView(content.getId(), content.getCode(), content.getContentType(), content.getStatus(),
                content.getVersion(), content.getPublishAt(), content.getUnpublishAt(), content.getDeletedAt(),
                translations);
    }

    public record ResolvedRoute(ContentView content, LanguageCode resolvedLanguage, boolean fallback) {
    }
}
