package com.asrevo.cvhome.content.service.binding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.errors.ContentErrors;
import com.asrevo.cvhome.content.model.HomeSectionKind;
import com.asrevo.cvhome.content.model.section.PersistableSection;
import com.asrevo.cvhome.content.model.section.ReadableSection;
import com.asrevo.cvhome.content.model.section.SectionMeta;
import com.asrevo.cvhome.content.service.ContentMapper;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.errors.FieldError;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Blocks on the store's home page.
 *
 * <p>
 * Everything type-specific lives in {@code meta}; the only column a section uses beyond the common ones is
 * {@code sort_order}, which is the page order. A section's copy is a heading and an optional subtitle, so it does
 * not require a body to publish.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class SectionBinding implements ContentTypeBinding<PersistableSection, ReadableSection> {

    private static final String KIND = "kind";

    private static final String TARGET_VALUE = "targetValue";

    private final MediaService media;

    @Override
    public ContentType type() {
        return ContentType.SECTION;
    }

    @Override
    public Class<PersistableSection> persistableClass() {
        return PersistableSection.class;
    }

    @Override
    public ReadableSection newReadable() {
        return new ReadableSection();
    }

    @Override
    public boolean requiresBody() {
        return false;
    }

    @Override
    public void apply(Content entity, PersistableSection dto) {
        entity.setMeta(JsonCodec.write(new SectionMeta(dto.getKind(), dto.getTargetValue(), dto.getMediaId(),
                dto.getItemLimit(), dto.getLayout(), dto.getCta())));
    }

    @Override
    public void populate(Content entity, ReadableSection dto) {
        SectionMeta meta = meta(entity);
        dto.setKind(meta.kind());
        dto.setTargetValue(meta.targetValue());
        dto.setMediaId(meta.mediaId());
        dto.setItemLimit(meta.itemLimit());
        dto.setLayout(meta.layout());
        dto.setCta(meta.cta());
        dto.setStatus(entity.getStatus());
        dto.setLocales(ContentMapper.locales(entity));
        dto.setAudit(ContentMapper.audit(entity));
        if (meta.mediaId() != null) {
            dto.setImageUrl(media.url(entity.getStoreMerchantId(), meta.mediaId()).orElse(null));
        }
    }

    @Override
    public String subtitle(Content entity, LanguageCode language) {
        SectionMeta meta = meta(entity);
        String kind = meta.kind() == null ? "—" : meta.kind().name().toLowerCase().replace('_', ' ');
        return meta.targetValue() == null || meta.targetValue().isBlank()
                ? kind
                : String.format("%s · %s", kind, meta.targetValue());
    }

    /**
     * A section that names no kind renders nothing, and the collection kinds render nothing without something to
     * collect — both are worth catching before the block reaches the home page rather than after.
     */
    @Override
    public List<FieldError> publishProblems(Content entity, ContentDescription source) {
        List<FieldError> problems = new ArrayList<>();
        SectionMeta meta = meta(entity);
        if (meta.kind() == null) {
            problems.add(FieldError.of(KIND, ContentErrors.PUBLISH_INCOMPLETE, "Section kind is required."));
            return problems;
        }
        if (meta.kind().needsTarget() && (meta.targetValue() == null || meta.targetValue().isBlank())) {
            problems.add(FieldError.of(TARGET_VALUE, ContentErrors.PUBLISH_INCOMPLETE,
                    String.format("A %s section needs something to point at.", meta.kind())));
        }
        if (meta.kind() == HomeSectionKind.IMAGE && meta.mediaId() == null) {
            problems.add(FieldError.of("mediaId", ContentErrors.PUBLISH_INCOMPLETE,
                    "An image section needs an image."));
        }
        return problems;
    }

    @Override
    public Map<String, Long> mediaReferences(Content entity) {
        Map<String, Long> refs = new LinkedHashMap<>();
        SectionMeta meta = meta(entity);
        if (meta.mediaId() != null) {
            refs.put("image", meta.mediaId());
        }
        return refs;
    }

    public static SectionMeta meta(Content entity) {
        SectionMeta m = JsonCodec.read(entity.getMeta(), SectionMeta.class);
        return m != null ? m : new SectionMeta(null, null, null, null, null, null);
    }

}
