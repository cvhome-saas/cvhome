package com.asrevo.cvhome.content.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.PageLayout;
import com.asrevo.cvhome.content.entity.PageLayoutRevision;
import com.asrevo.cvhome.content.errors.ContentConflictException;
import com.asrevo.cvhome.content.errors.ContentErrors;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.ContentRuleException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutMeta;
import com.asrevo.cvhome.content.model.layout.PageKind;
import com.asrevo.cvhome.content.model.layout.PersistableLayout;
import com.asrevo.cvhome.content.model.layout.PublishedLayout;
import com.asrevo.cvhome.content.model.layout.ReadableLayout;
import com.asrevo.cvhome.content.model.layout.ReadableRevisionRow;
import com.asrevo.cvhome.content.repository.PageLayoutRepository;
import com.asrevo.cvhome.content.repository.PageLayoutRevisionRepository;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.errors.FieldError;

import lombok.RequiredArgsConstructor;

/**
 * The layout document's lifecycle: load-or-default, draft saves under an optimistic version, atomic publish
 * with a whole-document revision snapshot, discard, restore.
 *
 * <p>
 * Every store always has a home layout — a row is materialized from {@link LayoutDefaults} on first touch, so
 * neither the builder nor the storefront ever handles a "no layout" branch.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class PageLayoutService {

    private final PageLayoutRepository layouts;

    private final PageLayoutRevisionRepository revisions;

    private final MediaService media;

    private final MediaUsageTracker usage;

    private final Clock clock;

    @Transactional
    public ReadableLayout get(StoreMerchantId store, PageKind page) {
        return readable(loadOrCreate(store, page));
    }

    @Transactional
    public ReadableLayout save(StoreMerchantId store, PageKind page, PersistableLayout body, String actor)
            throws InvalidContentRequestException, ContentConflictException {
        LayoutSupport.validate(body.document());
        String json = JsonCodec.write(body.document());
        if (json.getBytes(StandardCharsets.UTF_8).length > LayoutSupport.MAX_JSON_BYTES) {
            throw InvalidContentRequestException.layoutInvalid("The document exceeds the size budget.");
        }
        PageLayout entity = loadOrCreate(store, page);
        requireVersion(entity, body.baseVersion());
        entity.setDraft(json);
        entity.setDraftVersion(entity.getDraftVersion() + 1);
        touch(entity, actor);
        usage.replace(store, MediaOwnerKind.LAYOUT, ownerRef(entity), null, null, ownerTitle(page),
                LayoutSupport.mediaReferences(body.document()));
        return readable(entity);
    }

    /**
     * Copies the draft over the published document in one transaction and snapshots it as a revision. Blocks
     * only on what would be unaccountable — a media reference the library does not hold; everything catalog-
     * owned comes back as warnings on the result.
     */
    @Transactional
    public PublishedLayout publish(StoreMerchantId store, PageKind page, Integer baseVersion, String actor)
            throws ContentConflictException, ContentRuleException {
        PageLayout entity = loadOrCreate(store, page);
        requireVersion(entity, baseVersion);
        LayoutDocument draft = document(entity.getDraft());
        requireMedia(store, entity, draft);
        entity.setPublished(entity.getDraft());
        entity.setPublishedVersion(entity.getDraftVersion());
        entity.setPublishedAt(clock.instant());
        touch(entity, actor);
        PageLayoutRevision revision = new PageLayoutRevision();
        revision.setLayoutId(entity.getId());
        revision.setVersion(entity.getDraftVersion());
        revision.setSnapshot(entity.getPublished());
        revision.setPublishedBy(actor);
        revision.setDateCreated(clock.instant());
        revisions.save(revision);
        return new PublishedLayout(meta(entity), LayoutSupport.warnings(draft));
    }

    /** Throws away the draft: the published document (or the starter default) becomes the draft again. */
    @Transactional
    public ReadableLayout discard(StoreMerchantId store, PageKind page, Integer baseVersion, String actor)
            throws ContentConflictException {
        PageLayout entity = loadOrCreate(store, page);
        requireVersion(entity, baseVersion);
        String base = entity.getPublished() != null ? entity.getPublished()
                : JsonCodec.write(LayoutDefaults.starterHome());
        entity.setDraft(base);
        entity.setDraftVersion(entity.getDraftVersion() + 1);
        touch(entity, actor);
        usage.replace(store, MediaOwnerKind.LAYOUT, ownerRef(entity), null, null, ownerTitle(page),
                LayoutSupport.mediaReferences(document(base)));
        return readable(entity);
    }

    @Transactional
    public List<ReadableRevisionRow> revisions(StoreMerchantId store, PageKind page) {
        PageLayout entity = loadOrCreate(store, page);
        return revisions.findByLayoutIdOrderByVersionDesc(entity.getId()).stream()
                .map(r -> new ReadableRevisionRow(r.getVersion(), r.getDateCreated(), r.getPublishedBy()))
                .toList();
    }

    /** Restores a past published version into the draft; publishing it again is a separate, explicit step. */
    @Transactional
    public ReadableLayout restore(StoreMerchantId store, PageKind page, int version, String actor)
            throws ContentNotFoundException {
        PageLayout entity = loadOrCreate(store, page);
        PageLayoutRevision revision = revisions.findByLayoutIdAndVersion(entity.getId(), version)
                .orElseThrow(() -> ContentNotFoundException.byId((long) version, store.getId()));
        entity.setDraft(revision.getSnapshot());
        entity.setDraftVersion(entity.getDraftVersion() + 1);
        touch(entity, actor);
        usage.replace(store, MediaOwnerKind.LAYOUT, ownerRef(entity), null, null, ownerTitle(page),
                LayoutSupport.mediaReferences(document(revision.getSnapshot())));
        return readable(entity);
    }

    /** What the storefront serves: the published document, or the starter default before any publish. */
    @Transactional
    public LayoutDocument served(StoreMerchantId store, PageKind page, boolean draft) {
        PageLayout entity = loadOrCreate(store, page);
        if (draft) {
            return document(entity.getDraft());
        }
        return entity.getPublished() != null ? document(entity.getPublished()) : LayoutDefaults.starterHome();
    }

    private PageLayout loadOrCreate(StoreMerchantId store, PageKind page) {
        return layouts.findByStoreMerchantIdAndPage(store.getId(), page.name()).orElseGet(() -> {
            PageLayout entity = new PageLayout();
            entity.setStoreMerchantId(store.getId());
            entity.setPage(page.name());
            entity.setDraft(JsonCodec.write(LayoutDefaults.starterHome()));
            entity.setDateCreated(clock.instant());
            return layouts.save(entity);
        });
    }

    private void requireVersion(PageLayout entity, Integer baseVersion) throws ContentConflictException {
        if (!Objects.equals(baseVersion, entity.getDraftVersion())) {
            throw ContentConflictException.versionConflict(entity.getId(), baseVersion, entity.getDraftVersion());
        }
    }

    private void requireMedia(StoreMerchantId store, PageLayout entity, LayoutDocument draft)
            throws ContentRuleException {
        Map<String, Long> refs = LayoutSupport.mediaReferences(draft);
        Map<Long, String> known = media.urls(store, refs.values().stream().distinct().toList());
        List<FieldError> missing = refs.entrySet().stream()
                .filter(e -> !known.containsKey(e.getValue()))
                .map(e -> FieldError.of(e.getKey(), ContentErrors.MEDIA_NOT_FOUND,
                        String.format("Media %d is not in this store's library.", e.getValue())))
                .toList();
        if (!missing.isEmpty()) {
            throw ContentRuleException.publishIncomplete(entity.getId(), missing);
        }
    }

    private void touch(PageLayout entity, String actor) {
        entity.setLastModified(clock.instant());
        entity.setModifiedBy(actor);
    }

    private ReadableLayout readable(PageLayout entity) {
        return new ReadableLayout(document(entity.getDraft()), meta(entity));
    }

    private LayoutMeta meta(PageLayout entity) {
        boolean dirty = entity.getPublished() == null || !entity.getPublished().equals(entity.getDraft());
        return new LayoutMeta(entity.getDraftVersion(), entity.getPublishedVersion(), entity.getPublishedAt(),
                dirty);
    }

    private LayoutDocument document(String json) {
        return JsonCodec.read(json, LayoutDocument.class);
    }

    private String ownerRef(PageLayout entity) {
        return String.valueOf(entity.getId());
    }

    private String ownerTitle(PageKind page) {
        return page == PageKind.HOME ? "Home page layout" : String.format("%s layout", page.name());
    }

}
