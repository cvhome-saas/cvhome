package com.asrevo.cvhome.content.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The kind drives the console's filter tabs and the thumbnail it renders, so an SVG has to sort as a VECTOR
 * rather than as an image — it is a document that can carry markup, not a raster the browser can safely inline.
 */
class MediaKindTest {

    @ParameterizedTest
    @CsvSource({
        "image/svg+xml, VECTOR",
        "IMAGE/SVG+XML, VECTOR",
        "image/png, IMAGE",
        "image/webp, IMAGE",
        "video/mp4, VIDEO",
        "application/zip, ARCHIVE",
        "application/x-zip-compressed, ARCHIVE",
        "application/pdf, DOCUMENT",
    })
    void everyAcceptedTypeSortsIntoItsKind(String mimeType, MediaKind expected) {
        assertThat(MediaKind.ofMimeType(mimeType)).isEqualTo(expected);
    }

    @Test
    void anUnknownOrAbsentTypeIsADocument() {
        assertThat(MediaKind.ofMimeType(null)).isEqualTo(MediaKind.DOCUMENT);
        assertThat(MediaKind.ofMimeType("")).isEqualTo(MediaKind.DOCUMENT);
        assertThat(MediaKind.ofMimeType("application/octet-stream")).isEqualTo(MediaKind.DOCUMENT);
    }

}
