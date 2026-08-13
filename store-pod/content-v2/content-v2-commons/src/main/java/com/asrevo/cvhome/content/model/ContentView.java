package com.asrevo.cvhome.content.model;

import java.time.Instant;
import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;

public record ContentView(
        Long id,
        String code,
        ContentType type,
        ContentStatus status,
        long version,
        Instant publishAt,
        Instant unpublishAt,
        Instant deletedAt,
        List<TranslationView> translations
) {
    public record TranslationView(
            LanguageCode language,
            TranslationState state,
            String name,
            String title,
            String description,
            String slug,
            String metaTitle,
            String metaDescription,
            String metaKeywords,
            String canonicalUrl,
            boolean noIndex
    ) {
    }
}
