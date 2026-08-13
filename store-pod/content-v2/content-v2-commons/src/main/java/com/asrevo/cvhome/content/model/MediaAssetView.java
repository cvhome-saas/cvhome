package com.asrevo.cvhome.content.model;

import java.util.List;

public record MediaAssetView(
        Long id,
        String filename,
        String mimeType,
        MediaKind kind,
        long bytes,
        String checksum,
        Integer width,
        Integer height,
        Integer pageCount,
        MediaProcessingStatus status,
        List<MediaVariantView> variants
) {
    public record MediaVariantView(String name, String format, int width, int height, long bytes) {
    }
}
