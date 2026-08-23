package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentRevision;
import com.asrevo.cvhome.content.model.common.PersistableContent;
import com.asrevo.cvhome.content.model.common.ReadableRevision;
import com.asrevo.cvhome.content.repository.ContentRevisionRepository;
import com.asrevo.cvhome.content.support.JsonCodec;

import lombok.RequiredArgsConstructor;

/**
 * Immutable per-version snapshots. A snapshot is the readable DTO as JSON; restoring one replays it through the same
 * mapper a {@code PUT} uses, producing a new version rather than rewriting history.
 */
@Service
@RequiredArgsConstructor
public class RevisionService {

    private static final int KEEP = 50;

    private final ContentRevisionRepository repository;

    public void record(Content entity, PersistableContent readable, String actor) {
        ContentRevision r = new ContentRevision();
        r.setStoreMerchantId(entity.getStoreMerchantId().getId());
        r.setContentId(entity.getId());
        r.setVersion(entity.getVersion());
        r.setSnapshot(JsonCodec.write(readable));
        r.setAuthor(actor);
        repository.findByContentIdAndVersion(entity.getId(), entity.getVersion()).ifPresent(repository::delete);
        repository.save(r);
        List<ContentRevision> all = repository.findByContentIdOrderByVersionDesc(entity.getId());
        if (all.size() > KEEP) {
            repository.deleteAll(all.subList(KEEP, all.size()));
        }
    }

    public List<ReadableRevision> list(Long contentId) {
        return repository.findByContentIdOrderByVersionDesc(contentId).stream()
                .map(r -> new ReadableRevision(r.getVersion(), r.getAuthor(), r.getCreatedAt()))
                .toList();
    }

    public <P extends PersistableContent> Optional<P> snapshot(Long contentId, Integer version, Class<P> type) {
        return repository.findByContentIdAndVersion(contentId, version)
                .map(r -> JsonCodec.read(r.getSnapshot(), type));
    }

    public void forget(Long contentId) {
        repository.deleteByContentId(contentId);
    }

}
