package com.asrevo.cvhome.content.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.LayoutStyle;
import com.asrevo.cvhome.content.model.layout.LayoutVisibility;
import com.asrevo.cvhome.content.model.layout.PageKind;

/**
 * The starter home: what a brand-new store's page is, before anyone opens the builder. Designed to look
 * complete with zero merchant data — the hero is a text slide, and the data-driven sections (categories,
 * products) collapse on the storefront while they are empty and grow in as the catalogue fills.
 *
 * The copy is not code: it lives in {@code layout-defaults/messages*.properties}, one bundle per platform
 * language, and every language ships in the document's locale maps — the storefront then serves whichever
 * of them the store actually supports.
 */
public final class LayoutDefaults {

    /** Every language the platform ships; the storefront picks the store's own out of the map. */
    private static final List<String> LANGUAGES = List.of("en", "ar", "es", "fr", "ru");

    private static final String BUNDLE = "layout-defaults.messages";

    private static final String TITLE = "title";
    private static final String HEADING = "heading";
    private static final String LIMIT = "limit";
    private static final String SOURCE = "source";
    private static final String TYPE = "type";
    private static final String GRID = "grid";
    private static final String PRODUCTS = "products";
    private static final String MD = "md";
    private static final String DEFAULT_TONE = "default";

    private static final LayoutStyle CONTENT_MD = new LayoutStyle(MD, "content", DEFAULT_TONE);

    private static final LayoutVisibility VISIBLE = new LayoutVisibility(false, null);

    private LayoutDefaults() {
    }

    public static LayoutDocument starterHome() {
        return new LayoutDocument(LayoutDocument.CURRENT_SCHEMA_VERSION, PageKind.HOME, List.of(
                new LayoutSection("sec-start-hero", "hero", "minimal", Map.of("height", MD), null,
                        Map.of(HEADING, text("hero.heading"), "subheading", text("hero.subheading")),
                        new LayoutStyle("lg", "full", DEFAULT_TONE), VISIBLE, null, null),
                new LayoutSection("sec-start-categories", "categories", GRID, Map.of(LIMIT, 6), null,
                        Map.of(TITLE, text("categories.title")),
                        CONTENT_MD, VISIBLE, null, null),
                new LayoutSection("sec-start-featured", PRODUCTS, GRID,
                        Map.of(SOURCE, Map.of(TYPE, "group", "code", "FEATURED_ITEMS"), LIMIT, 8), null,
                        Map.of(TITLE, text("featured.title")),
                        CONTENT_MD, VISIBLE, null, null),
                new LayoutSection("sec-start-newest", PRODUCTS, "rail",
                        Map.of(SOURCE, Map.of(TYPE, "newest"), LIMIT, 8), null,
                        Map.of(TITLE, text("newest.title")),
                        CONTENT_MD, VISIBLE, null, null),
                new LayoutSection("sec-start-welcome", "richtext", "centered", Map.of(), null,
                        Map.of("body", text("welcome.body")),
                        CONTENT_MD, VISIBLE, null, null),
                new LayoutSection("sec-start-newsletter", "newsletter", "inline", Map.of(), null,
                        Map.of(HEADING, text("newsletter.heading")),
                        CONTENT_MD, VISIBLE, null, null)));
    }

    /** The key's copy in every platform language, as the layout document's locale map. */
    private static Map<String, String> text(String key) {
        Map<String, String> byLanguage = new LinkedHashMap<>();
        for (String language : LANGUAGES) {
            // no-fallback control: "en" must resolve to the base bundle, never via the JVM's default locale
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, Locale.of(language),
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
            byLanguage.put(language, bundle.getString(key));
        }
        return byLanguage;
    }

}
