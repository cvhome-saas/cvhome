package com.asrevo.cvhome.content.model;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContentWriteRequest(
        @NotBlank @Size(max = 100) String code,
        @NotNull ContentType type,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String title,
        String description,
        @Size(max = 255) String slug,
        @Size(max = 255) String metaTitle,
        @Size(max = 500) String metaDescription,
        @Size(max = 500) String metaKeywords,
        @Size(max = 1000) String canonicalUrl,
        boolean noIndex,
        Instant publishAt,
        Instant unpublishAt
) {
}
