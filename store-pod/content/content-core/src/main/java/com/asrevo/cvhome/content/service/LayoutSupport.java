package com.asrevo.cvhome.content.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asrevo.cvhome.content.errors.ContentErrors;
import com.asrevo.cvhome.content.errors.InvalidContentRequestException;
import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutItem;
import com.asrevo.cvhome.content.model.layout.LayoutKinds;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.support.HtmlSanitizer;
import com.asrevo.cvhome.errors.FieldError;

/**
 * Structural rules of a layout document, in one place: what a save refuses outright, and how media references
 * are found. Per-kind prop shapes are the storefront catalogue's business, not validated here — the boundary is
 * "nothing unrenderable or unaccountable gets stored", not "the props are pretty".
 */
public final class LayoutSupport {

    /** A generous whole-document budget; a page that exceeds it is holding images inline by mistake. */
    public static final int MAX_JSON_BYTES = 256 * 1024;

    /** The one prop key that carries a media-library reference, on sections and items alike. */
    public static final String MEDIA_ID = "mediaId";

    /** The one section kind whose copy the storefront injects as HTML rather than rendering as text. */
    private static final String RICHTEXT = "richtext";

    private static final String BODY = "body";

    private LayoutSupport() {
    }

    /**
     * Refuses a document the builder should never produce: wrong schema, too many blocks, duplicate or missing
     * ids, a kind nothing renders.
     */
    public static void validate(LayoutDocument document) throws InvalidContentRequestException {
        if (document == null) {
            throw InvalidContentRequestException.layoutInvalid("The document is required.");
        }
        if (document.schemaVersion() == null || document.schemaVersion() != LayoutDocument.CURRENT_SCHEMA_VERSION) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("Unsupported schemaVersion %s.", document.schemaVersion()));
        }
        List<LayoutSection> sections = document.sections();
        if (sections.size() > LayoutDocument.MAX_SECTIONS) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("A page holds at most %d sections.", LayoutDocument.MAX_SECTIONS));
        }
        Set<String> ids = new HashSet<>();
        for (LayoutSection section : sections) {
            validateSection(section, ids);
        }
    }

    private static void validateSection(LayoutSection section, Set<String> ids)
            throws InvalidContentRequestException {
        if (section.id() == null || section.id().isBlank()) {
            throw InvalidContentRequestException.layoutInvalid("Every section needs an id.");
        }
        if (!ids.add(section.id())) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("Duplicate section id %s.", section.id()));
        }
        if (section.kind() == null || !LayoutKinds.KNOWN.contains(section.kind())) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("Unknown section kind %s.", section.kind()));
        }
        if (section.items().size() > LayoutSection.MAX_ITEMS) {
            throw InvalidContentRequestException.layoutInvalid(
                    String.format("A section holds at most %d items.", LayoutSection.MAX_ITEMS));
        }
        validateItems(section);
    }

    private static void validateItems(LayoutSection section) throws InvalidContentRequestException {
        Set<String> itemIds = new HashSet<>();
        for (LayoutItem item : section.items()) {
            if (item.id() == null || item.id().isBlank() || !itemIds.add(item.id())) {
                throw InvalidContentRequestException.layoutInvalid(
                        String.format("Items of section %s need unique ids.", section.id()));
            }
        }
    }

    /**
     * The document with every rich-text body run through the platform's HTML allow-list. The storefront injects
     * those bodies as markup, so the write path owns making them safe — the same sanitised-on-write rule every
     * CMS body already follows. All other layout copy renders as text and passes through untouched.
     */
    public static LayoutDocument sanitized(LayoutDocument document) {
        List<LayoutSection> sections = document.sections().stream().map(LayoutSupport::sanitizedSection).toList();
        return new LayoutDocument(document.schemaVersion(), document.page(), sections);
    }

    private static LayoutSection sanitizedSection(LayoutSection section) {
        Map<String, String> body = section.text().get(BODY);
        if (!RICHTEXT.equals(section.kind()) || body == null) {
            return section;
        }
        Map<String, String> clean = new LinkedHashMap<>();
        body.forEach((locale, html) -> clean.put(locale, HtmlSanitizer.clean(html)));
        Map<String, Map<String, String>> text = new LinkedHashMap<>(section.text());
        text.put(BODY, clean);
        return new LayoutSection(section.id(), section.kind(), section.variant(), section.props(),
                section.items(), text, section.style(), section.visibility(), section.anchor(), section.locked());
    }

    /**
     * Every media reference in the document, keyed for the usage index: {@code <sectionId>} for a section's own
     * image, {@code <sectionId>/<itemId>} for an item's. Walks any {@code mediaId} prop.
     */
    public static Map<String, Long> mediaReferences(LayoutDocument document) {
        Map<String, Long> refs = new LinkedHashMap<>();
        for (LayoutSection section : document.sections()) {
            Long own = mediaId(section.props());
            if (own != null) {
                refs.put(section.id(), own);
            }
            for (LayoutItem item : section.items()) {
                Long ref = mediaId(item.props());
                if (ref != null) {
                    refs.put(String.format("%s/%s", section.id(), item.id()), ref);
                }
            }
        }
        return refs;
    }

    /**
     * Non-blocking publish findings. Catalog-owned references (a product group, a category) are not this
     * service's to assert, and the storefront collapses a section whose data resolves empty — so a missing
     * source is a warning the merchant sees, never a wall.
     */
    public static List<FieldError> warnings(LayoutDocument document) {
        List<FieldError> warnings = new ArrayList<>();
        for (LayoutSection section : document.sections()) {
            switch (section.kind()) {
                case "products" -> {
                    if (!(section.props().get("source") instanceof Map<?, ?> source) || source.get("type") == null) {
                        warnings.add(FieldError.of(section.id(), ContentErrors.PUBLISH_INCOMPLETE,
                                "The products section has no source and will not show anything."));
                    }
                }
                case "faq" -> {
                    if (blank(section.props().get("group"))) {
                        warnings.add(FieldError.of(section.id(), ContentErrors.PUBLISH_INCOMPLETE,
                                "The FAQ section names no group and will show the whole FAQ."));
                    }
                }
                case "hero" -> {
                    if (section.items().isEmpty() && section.text().isEmpty()) {
                        warnings.add(FieldError.of(section.id(), ContentErrors.PUBLISH_INCOMPLETE,
                                "The hero has neither slides nor text."));
                    }
                }
                default -> {
                    // other kinds render from their own props/text; an empty one collapses at render
                }
            }
        }
        return warnings;
    }

    private static boolean blank(Object value) {
        return !(value instanceof String s) || s.isBlank();
    }

    private static Long mediaId(Map<String, Object> props) {
        return props.get(MEDIA_ID) instanceof Number n ? n.longValue() : null;
    }

}
