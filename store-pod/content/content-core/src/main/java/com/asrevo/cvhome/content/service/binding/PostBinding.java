package com.asrevo.cvhome.content.service.binding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.entity.Content;
import com.asrevo.cvhome.content.entity.ContentDescription;
import com.asrevo.cvhome.content.model.post.PersistablePost;
import com.asrevo.cvhome.content.model.post.PostMeta;
import com.asrevo.cvhome.content.model.post.ReadablePost;
import com.asrevo.cvhome.content.service.ContentMapper;
import com.asrevo.cvhome.content.service.ContentTypeBinding;
import com.asrevo.cvhome.content.service.MediaService;
import com.asrevo.cvhome.content.support.JsonCodec;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import lombok.RequiredArgsConstructor;

/**
 * Blog posts ({@code /blog/<slug>}). Hero image, categories, tags, author and featured flag live in {@code meta}.
 */
@Component
@RequiredArgsConstructor
public class PostBinding implements ContentTypeBinding<PersistablePost, ReadablePost> {

    private static final int WORDS_PER_MINUTE = 220;

    private final MediaService media;

    @Override
    public ContentType type() {
        return ContentType.POST;
    }

    @Override
    public Class<PersistablePost> persistableClass() {
        return PersistablePost.class;
    }

    @Override
    public ReadablePost newReadable() {
        return new ReadablePost();
    }

    @Override
    public void apply(Content entity, PersistablePost dto) {
        List<String> tags = dto.getTags() == null ? List.of()
                : dto.getTags().stream().map(String::trim).filter(t -> !t.isEmpty()).distinct().toList();
        entity.setMeta(JsonCodec.write(new PostMeta(dto.getHeroMediaId(),
                dto.getCategoryIds() == null ? List.of() : dto.getCategoryIds(), tags, dto.getAuthorName(),
                dto.isFeatured())));
    }

    @Override
    public void populate(Content entity, ReadablePost dto) {
        PostMeta meta = meta(entity);
        dto.setHeroMediaId(meta.heroMediaId());
        dto.setCategoryIds(meta.categoryIds() == null ? new ArrayList<>() : new ArrayList<>(meta.categoryIds()));
        dto.setTags(meta.tags() == null ? new ArrayList<>() : new ArrayList<>(meta.tags()));
        dto.setAuthorName(meta.authorName());
        dto.setFeatured(meta.featured());
        dto.setStatus(entity.getStatus());
        dto.setLocales(ContentMapper.locales(entity));
        dto.setAudit(ContentMapper.audit(entity));
        dto.setReadingMinutes(readingMinutes(entity));
        dto.setHeroMediaUrl(media.url(entity.getStoreMerchantId(), meta.heroMediaId()).orElse(null));
    }

    @Override
    public String subtitle(Content entity, LanguageCode language) {
        return storefrontPath(entity);
    }

    @Override
    public String storefrontPath(Content entity) {
        return String.format("/blog/%s", entity.getCode());
    }

    @Override
    public Map<String, Long> mediaReferences(Content entity) {
        Map<String, Long> refs = new LinkedHashMap<>();
        Long hero = meta(entity).heroMediaId();
        if (hero != null) {
            refs.put("hero", hero);
        }
        return refs;
    }

    public static PostMeta meta(Content entity) {
        PostMeta m = JsonCodec.read(entity.getMeta(), PostMeta.class);
        return m != null ? m : new PostMeta(null, List.of(), List.of(), null, false);
    }

    public static int readingMinutes(Content entity) {
        int words = 0;
        for (ContentDescription d : entity.getDescriptions()) {
            if (d.getDescription() != null) {
                String text = d.getDescription().replaceAll("<[^>]+>", " ").trim();
                int count = text.isEmpty() ? 0 : text.split("\\s+").length;
                words = Math.max(words, count);
            }
        }
        return words == 0 ? 0 : Math.max(1, (int) Math.ceil(words / (double) WORDS_PER_MINUTE));
    }

}
