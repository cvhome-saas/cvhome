package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.MediaUsageRow;
import com.asrevo.cvhome.content.repository.MediaUsageRepository;

import lombok.RequiredArgsConstructor;

/**
 * Keeps the reverse index "which items use this asset" current: rebuilt for an item on every save.
 */
@Service
@RequiredArgsConstructor
public class MediaUsageTracker {

    private final MediaUsageRepository repository;

    public void record(Content item, Map<String, Long> references) {
        repository.deleteByContentId(item.getId());
        List<MediaUsageRow> rows = new ArrayList<>();
        references.forEach((field, assetId) -> {
            if (assetId != null) {
                MediaUsageRow r = new MediaUsageRow();
                r.setAssetId(assetId);
                r.setContentId(item.getId());
                r.setContentType(item.getContentType());
                r.setField(field);
                rows.add(r);
            }
        });
        if (!rows.isEmpty()) {
            repository.saveAll(rows);
        }
    }

    public void forget(Content item) {
        repository.deleteByContentId(item.getId());
    }

}
