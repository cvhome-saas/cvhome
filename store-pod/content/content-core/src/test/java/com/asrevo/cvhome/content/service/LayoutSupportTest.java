package com.asrevo.cvhome.content.service;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutItem;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.PageKind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The structural gate on a layout document: what a save refuses, and how media references are collected for
 * the usage index. Per-kind prop shapes are deliberately not validated — that truth lives in the storefront's
 * section catalogue.
 */
class LayoutSupportTest {
    private static final String SEC_A = "a";
    private static final String HERO = "hero";
    private static final String MEDIA_ID = "mediaId";
    private static final String HOLOGRAM = "hologram";
    private static final String IMG = "img";
    private static final String ITEM_P = "p";


    private static LayoutSection section(String id, String kind, Map<String, Object> props, List<LayoutItem> items) {
        return new LayoutSection(id, kind, null, props, items, null, null, null, null, null);
    }

    private static LayoutDocument document(LayoutSection... sections) {
        return new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(sections));
    }

    @Test
    void aWellFormedDocumentPasses() throws InvalidContentRequestException {
        LayoutSupport.validate(document(
                section(SEC_A, HERO, Map.of(), List.of(new LayoutItem("i1", Map.of(MEDIA_ID, 5), null))),
                section("b", "richtext", Map.of(), null)));
    }

    @Test
    void anUnknownKindIsRefused() {
        assertThatThrownBy(() -> LayoutSupport.validate(document(section(SEC_A, HOLOGRAM, Map.of(), null))))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining(HOLOGRAM);
    }

    @Test
    void duplicateSectionIdsAreRefused() {
        assertThatThrownBy(() -> LayoutSupport.validate(document(
                section(SEC_A, HERO, Map.of(), null), section(SEC_A, "usp", Map.of(), null))))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("Duplicate");
    }

    @Test
    void theWrongSchemaVersionIsRefused() {
        assertThatThrownBy(() -> LayoutSupport.validate(
                new LayoutDocument(99, PageKind.HOME, List.of())))
                .isInstanceOf(InvalidContentRequestException.class)
                .hasMessageContaining("schemaVersion");
    }

    @Test
    void mediaReferencesAreCollectedFromSectionsAndItems() {
        Map<String, Long> refs = LayoutSupport.mediaReferences(document(
                section(IMG, "image", Map.of(MEDIA_ID, 7), null),
                section(HERO, HERO, Map.of(), List.of(
                        new LayoutItem("s1", Map.of(MEDIA_ID, 8), null),
                        new LayoutItem("s2", Map.of(), null)))));

        assertThat(refs).containsExactly(Map.entry(IMG, 7L), Map.entry("hero/s1", 8L));
    }

    @Test
    void aSourcelessProductsSectionIsAWarningNotAWall() {
        var warnings = LayoutSupport.warnings(document(section(ITEM_P, "products", Map.of(), null)));

        assertThat(warnings).singleElement()
                .satisfies(w -> assertThat(w.field()).isEqualTo(ITEM_P));
    }

}
