package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.MediaUsageRow;
import com.asrevo.cvhome.content.model.MediaOwnerKind;
import com.asrevo.cvhome.content.repository.MediaUsageRepository;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Keeps the reverse index "which owners use this asset" current: rebuilt for an owner on every save.
 *
 * <p>
 * Every write is a <em>replace</em> of the owner's whole reference set rather than an add/remove pair. That makes
 * a retry, a re-save and a partial failure all converge on the same rows, which matters because catalog drives
 * this over HTTP ({@code ExternalMediaService}) where any of the three can happen.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class MediaUsageTracker {

    private final MediaUsageRepository repository;

    /**
     * Replaces the references held by a content item. Delegates to
     * {@link #replace(StoreMerchantId, MediaOwnerKind, String, ContentType, Long, String, Map)}.
     */
    public void record(Content item, Map<String, Long> references) {
        replace(item.getStoreMerchantId(), MediaOwnerKind.CONTENT, String.valueOf(item.getId()),
                item.getContentType(), item.getId(), null, references);
    }

    public void forget(Content item) {
        forget(MediaOwnerKind.CONTENT, String.valueOf(item.getId()));
    }

    /**
     * Replaces every reference held by {@code (ownerKind, ownerRef)} with {@code references}. An empty map
     * therefore releases the owner's assets.
     */
    @Transactional
    public void replace(StoreMerchantId store, MediaOwnerKind ownerKind, String ownerRef, ContentType contentType,
                        Long contentId, String ownerTitle, Map<String, Long> references) {
        repository.deleteByOwnerKindAndOwnerRef(ownerKind, ownerRef);
        List<MediaUsageRow> rows = new ArrayList<>();
        references.forEach((field, assetId) -> {
            if (assetId != null) {
                MediaUsageRow r = new MediaUsageRow();
                r.setAssetId(assetId);
                r.setOwnerKind(ownerKind);
                r.setOwnerRef(ownerRef);
                r.setOwnerTitle(ownerTitle);
                r.setContentType(contentType);
                r.setContentId(contentId);
                r.setField(field);
                rows.add(r);
            }
        });
        if (!rows.isEmpty()) {
            repository.saveAll(rows);
        }
    }

    @Transactional
    public void forget(MediaOwnerKind ownerKind, String ownerRef) {
        repository.deleteByOwnerKindAndOwnerRef(ownerKind, ownerRef);
    }

}
