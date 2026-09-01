package com.asrevo.cvhome.content.service;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.entity.SectionPreset;
import com.asrevo.cvhome.content.errors.ContentNotFoundException;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.layout.LayoutKinds;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.PersistableSavedSection;
import com.asrevo.cvhome.content.model.layout.SavedSection;
import com.asrevo.cvhome.content.repository.SectionPresetRepository;
import com.asrevo.cvhome.content.support.JsonCodec;

import lombok.RequiredArgsConstructor;

/**
 * The merchant's own section library. Presets are snapshots — saving keeps a copy of the section as it is now,
 * and the builder re-ids everything on insert, so a preset can never be edited or broken from a page.
 */
@Service
@RequiredArgsConstructor
public class SectionPresetService {

    /** Enough for a real library, small enough that an automated loop cannot fill the table. */
    private static final int MAX_PRESETS = 100;

    private final SectionPresetRepository presets;

    private final Clock clock;

    @Transactional
    public List<SavedSection> list(StoreMerchantId store) {
        return presets.findByStoreMerchantIdOrderByDateCreatedDesc(store.getId()).stream()
                .map(this::readable).toList();
    }

    @Transactional
    public SavedSection save(StoreMerchantId store, PersistableSavedSection body, String actor)
            throws InvalidContentRequestException {
        LayoutSection section = body.section();
        if (section.kind() == null || !LayoutKinds.KNOWN.contains(section.kind())) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("Unknown section kind %s.", section.kind()));
        }
        if (section.items().size() > LayoutSection.MAX_ITEMS) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("A section holds at most %d items.", LayoutSection.MAX_ITEMS));
        }
        String snapshot = JsonCodec.write(section);
        if (snapshot.getBytes(StandardCharsets.UTF_8).length > LayoutSupport.MAX_JSON_BYTES) {
            throw InvalidContentRequestException.layoutInvalid("The section exceeds the size budget.");
        }
        if (presets.countByStoreMerchantId(store.getId()) >= MAX_PRESETS) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("The section library holds at most %d presets.", MAX_PRESETS));
        }
        SectionPreset entity = new SectionPreset();
        entity.setStoreMerchantId(store.getId());
        entity.setName(body.name());
        entity.setKind(section.kind());
        entity.setSnapshot(snapshot);
        entity.setDateCreated(clock.instant());
        entity.setModifiedBy(actor);
        return readable(presets.save(entity));
    }

    @Transactional
    public void delete(StoreMerchantId store, Long id) throws ContentNotFoundException {
        SectionPreset entity = presets.findByIdAndStoreMerchantId(id, store.getId())
                .orElseThrow(() -> ContentNotFoundException.byId(id, store.getId()));
        presets.delete(entity);
    }

    private SavedSection readable(SectionPreset entity) {
        return new SavedSection(entity.getId(), entity.getName(), entity.getKind(),
                JsonCodec.read(entity.getSnapshot(), LayoutSection.class), entity.getDateCreated());
    }

}
