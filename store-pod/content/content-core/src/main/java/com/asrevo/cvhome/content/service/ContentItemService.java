package com.asrevo.cvhome.content.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;
import com.asrevo.cvhome.content.model.common.BulkRequest;
import com.asrevo.cvhome.content.model.common.BulkResult;
import com.asrevo.cvhome.content.model.common.ContentTranslation;
import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.content.model.common.PublishRequest;
import com.asrevo.cvhome.content.model.common.ReadableContentRow;
import com.asrevo.cvhome.content.model.common.ReadableContentRowList;
import com.asrevo.cvhome.content.model.common.ReadableRevision;
import com.asrevo.cvhome.content.model.common.SavedContent;
import com.asrevo.cvhome.content.repository.ContentRepository;
import com.asrevo.cvhome.content.repository.ContentSpecifications;
import com.asrevo.cvhome.content.repository.ContentStatusAuditRepository;
import com.asrevo.cvhome.errors.BaseException;

import lombok.RequiredArgsConstructor;

/**
 * Everything a workflow content item does that does not depend on its type: list, read, create, update, delete,
 * status transitions, revisions, per-locale writes and bulk. The type-specific part is delegated to the
 * {@link ContentTypeBinding} the caller passes in.
 */
@Service
@RequiredArgsConstructor
public class ContentItemService {

    private final ContentRepository repository;

    private final ContentStatusAuditRepository auditRepository;

    private final PublishingService publishing;

    private final RevisionService revisions;

    private final RedirectService redirects;

    private final Clock clock;

    // ---------------------------------------------------------------- reads

    @Transactional(readOnly = true)
    public ReadableContentRowList list(ContentTypeBinding<?, ?> binding, StoreMerchantId store, LanguageCode language,
                                       ListQuery query, Pageable pageable) {
        Specification<Content> spec = ContentSpecifications.forStoreAndType(store, binding.type())
                .and(ContentSpecifications.withStatus(query.status()))
                .and(ContentSpecifications.withLocaleState(query.locale(), query.state()))
                .and(ContentSpecifications.search(query.q()));
        Page<Content> page = repository.findAll(spec, mapSort(pageable));
        ReadableContentRowList out = new ReadableContentRowList();
        out.setTotalPages(page.getTotalPages());
        out.setSize(page.getNumberOfElements());
        out.setTotalElements(page.getTotalElements());
        out.setRecordsFiltered(page.getNumberOfElements());
        out.setPageNumber(page.getNumber());
        List<ReadableContentRow> rows = new ArrayList<>();
        for (Content c : page.getContent()) {
            rows.add(ContentMapper.row(c, language, binding.subtitle(c, language)));
        }
        out.setContent(rows);
        return out;
    }

    @Transactional(readOnly = true)
    public <P extends PersistableContent, R extends P> R get(ContentTypeBinding<P, R> binding, Long id,
                                                             StoreMerchantId store) throws ContentNotFoundException {
        return toReadable(binding, load(binding, id, store));
    }

    @Transactional(readOnly = true)
    public Content load(ContentTypeBinding<?, ?> binding, Long id, StoreMerchantId store)
            throws ContentNotFoundException {
        Content c = repository.findByIdAndStore(id, store).orElseThrow(() -> ContentNotFoundException.byId(id, store));
        if (c.getContentType() != binding.type()) {
            throw ContentNotFoundException.byId(id, store);
        }
        return c;
    }

    public <P extends PersistableContent, R extends P> R toReadable(ContentTypeBinding<P, R> binding, Content c) {
        R dto = binding.newReadable();
        ContentMapper.populateCommon(c, dto);
        binding.populate(c, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public boolean slugAvailable(StoreMerchantId store, String slug, Long excludeId) {
        return excludeId == null
                ? !repository.existsByStoreMerchantIdAndCode(store, slug)
                : !repository.existsByStoreMerchantIdAndCodeAndIdNot(store, slug, excludeId);
    }

    @Transactional(readOnly = true)
    public List<ReadableRevision> revisions(ContentTypeBinding<?, ?> binding, Long id, StoreMerchantId store)
            throws ContentNotFoundException {
        load(binding, id, store);
        return revisions.list(id);
    }

    // --------------------------------------------------------------- writes

    @Transactional
    public <P extends PersistableContent, R extends P> SavedContent create(ContentTypeBinding<P, R> binding, P dto,
                                                                           StoreMerchantId store,
                                                                           LanguageCode language, String actor)
            throws ContentConflictException, ContentRuleException {
        if (repository.existsByStoreMerchantIdAndCode(store, dto.getSlug())) {
            throw ContentConflictException.slugDuplicate(binding.type().name(), dto.getSlug(), store);
        }
        Content c = new Content();
        c.setStoreMerchantId(store);
        c.setContentType(binding.type());
        c.setStatus(ContentStatus.DRAFT);
        c.setVisible(false);
        c.setCreatedBy(actor);
        c.setUpdatedBy(actor);
        ContentMapper.applyCommon(c, dto);
        ContentMapper.applyTranslations(c, dto.getTranslations(), language, binding.requiresBody());
        binding.apply(c, dto);
        c = repository.saveAndFlush(c);
        binding.afterSave(c);
        revisions.record(c, toReadable(binding, c), actor);
        return new SavedContent(c.getId(), c.getStatus(), c.getVersion());
    }

    @Transactional
    public <P extends PersistableContent, R extends P> SavedContent update(ContentTypeBinding<P, R> binding, Long id,
                                                                           P dto, StoreMerchantId store,
                                                                           LanguageCode language, String actor)
            throws ContentNotFoundException, ContentConflictException, ContentRuleException {
        Content c = load(binding, id, store);
        if (dto.getVersion() != null && !Objects.equals(dto.getVersion(), c.getVersion())) {
            throw ContentConflictException.versionConflict(id, dto.getVersion(), c.getVersion());
        }
        if (!c.getCode().equals(dto.getSlug())
                && repository.existsByStoreMerchantIdAndCodeAndIdNot(store, dto.getSlug(), id)) {
            throw ContentConflictException.slugDuplicate(binding.type().name(), dto.getSlug(), store);
        }
        String oldPath = binding.storefrontPath(c);
        boolean wasPublished = c.getStatus() == ContentStatus.PUBLISHED;
        ContentMapper.applyCommon(c, dto);
        boolean sourceChanged = ContentMapper.applyTranslations(c, dto.getTranslations(), language,
                binding.requiresBody());
        if (sourceChanged && wasPublished) {
            markOthersStale(c, language);
        }
        binding.apply(c, dto);
        c.setUpdatedBy(actor);
        c = repository.saveAndFlush(c);
        binding.afterSave(c);
        String newPath = binding.storefrontPath(c);
        if (wasPublished && oldPath != null && !oldPath.equals(newPath)) {
            redirects.moved(store, oldPath, newPath);
        }
        revisions.record(c, toReadable(binding, c), actor);
        return new SavedContent(c.getId(), c.getStatus(), c.getVersion());
    }

    /**
     * Writes one locale without touching the others — the translator's path. A blank title and body removes it.
     */
    @Transactional
    public <P extends PersistableContent, R extends P> SavedContent updateTranslation(
            ContentTypeBinding<P, R> binding, Long id, LanguageCode locale, ContentTranslation translation,
            StoreMerchantId store, String actor) throws ContentNotFoundException {
        Content c = load(binding, id, store);
        translation.setLanguage(locale);
        List<ContentTranslation> all = new ArrayList<>(ContentMapper.translations(c));
        all.removeIf(t -> locale.equals(t.getLanguage()));
        all.add(translation);
        ContentMapper.applyTranslations(c, all, null, binding.requiresBody());
        c.setUpdatedBy(actor);
        c = repository.saveAndFlush(c);
        revisions.record(c, toReadable(binding, c), actor);
        return new SavedContent(c.getId(), c.getStatus(), c.getVersion());
    }

    @Transactional
    public <P extends PersistableContent, R extends P> SavedContent transition(ContentTypeBinding<P, R> binding,
                                                                               Long id, StoreMerchantId store,
                                                                               ContentStatus target,
                                                                               PublishRequest request,
                                                                               LanguageCode language, String actor)
            throws ContentNotFoundException, ContentRuleException, InvalidContentRequestException {
        Content c = load(binding, id, store);
        publishing.transition(c, target, request, language, binding, actor, null);
        c.setUpdatedBy(actor);
        c = repository.saveAndFlush(c);
        binding.afterSave(c);
        return new SavedContent(c.getId(), c.getStatus(), c.getVersion());
    }

    @Transactional
    public <P extends PersistableContent, R extends P> SavedContent restore(ContentTypeBinding<P, R> binding, Long id,
                                                                            Integer version, StoreMerchantId store,
                                                                            LanguageCode language, String actor)
            throws ContentNotFoundException, ContentConflictException, ContentRuleException {
        Content c = load(binding, id, store);
        P snapshot = revisions.snapshot(id, version, binding.persistableClass())
                .orElseThrow(() -> ContentNotFoundException.byId(id, store));
        snapshot.setVersion(c.getVersion());
        return update(binding, id, snapshot, store, language, actor);
    }

    @Transactional
    public void delete(ContentTypeBinding<?, ?> binding, Long id, StoreMerchantId store, boolean force)
            throws ContentNotFoundException, ContentConflictException {
        Content c = load(binding, id, store);
        binding.beforeDelete(c, force);
        revisions.forget(id);
        auditRepository.deleteByContentId(id);
        repository.delete(c);
        binding.afterDelete(c);
    }

    /**
     * Applies one action to many ids; never fails the batch on one bad id.
     */
    @Transactional
    public <P extends PersistableContent, R extends P> List<BulkResult> bulk(ContentTypeBinding<P, R> binding,
                                                                             BulkRequest request,
                                                                             StoreMerchantId store,
                                                                             LanguageCode language, String actor)
            throws InvalidContentRequestException {
        if (request.getIds().size() > BulkRequest.MAX_IDS) {
            throw InvalidContentRequestException.bulkTooLarge(request.getIds().size(), BulkRequest.MAX_IDS);
        }
        List<BulkResult> results = new ArrayList<>();
        for (Long id : request.getIds()) {
            try {
                switch (request.getAction()) {
                    case PUBLISH -> transition(binding, id, store, ContentStatus.PUBLISHED, null, language, actor);
                    case UNPUBLISH -> transition(binding, id, store, ContentStatus.DRAFT, null, language, actor);
                    case ARCHIVE -> transition(binding, id, store, ContentStatus.ARCHIVED, null, language, actor);
                    case DELETE -> delete(binding, id, store, false);
                    default -> throw new IllegalArgumentException(String.format("Unknown bulk action %s", request.getAction()));
                }
                results.add(new BulkResult(id, true, null, null));
            } catch (BaseException e) {
                results.add(new BulkResult(id, false, e.payload().errorCode().code(), e.getMessage()));
            }
        }
        return results;
    }

    // -------------------------------------------------------------- helpers

    private void markOthersStale(Content c, LanguageCode source) {
        for (ContentDescription d : c.getDescriptions()) {
            if (!d.getLanguageCode().equals(source) && d.getState() == TranslationState.TRANSLATED) {
                d.setState(TranslationState.STALE);
            }
        }
    }

    /**
     * Maps the public sort keys onto entity paths. Title lives on the per-locale row, so it sorts by slug.
     */
    static Pageable mapSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "auditSection.dateModified"));
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (Sort.Order o : pageable.getSort()) {
            String p = switch (o.getProperty()) {
                case "updatedAt" -> "auditSection.dateModified";
                case "createdAt" -> "auditSection.dateCreated";
                case "title", "slug" -> "code";
                case "status" -> "status";
                case "publishAt" -> "publishAt";
                case "sortOrder" -> "sortOrder";
                default -> "auditSection.dateModified";
            };
            orders.add(new Sort.Order(o.getDirection(), p));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    public Clock clock() {
        return clock;
    }

}
