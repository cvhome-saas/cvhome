package com.asrevo.cvhome.content.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.asrevo.cvhome.content.model.layout.LayoutDocument;
import com.asrevo.cvhome.content.model.layout.LayoutSection;
import com.asrevo.cvhome.content.model.layout.LayoutStyle;
import com.asrevo.cvhome.content.model.layout.LayoutVisibility;
import com.asrevo.cvhome.content.model.layout.PageKind;

import lombok.SneakyThrows;

/**
 * The starter home: what a brand-new store's page is, before anyone opens the builder. Designed to look
 * complete with zero merchant data — the hero is a text slide, and the data-driven sections (categories,
 * products) collapse on the storefront while they are empty and grow in as the catalogue fills.
 *
 * The copy is not code: it lives in {@code layout-defaults/messages*.properties}, one file per platform
 * language over the English base, and every language ships in the document's locale maps — the storefront then
 * serves whichever of them the store actually supports.
 *
 * <p>
 * The files are read as plain classpath resources, not through {@link java.util.ResourceBundle}: a native image
 * includes the JDK's bundle support only for the locales it was built with, so the first native load test could not
 * find the bundle at all ("Can't find bundle for base name layout-defaults.messages") and, found, would have had
 * English for every language. Resources behave the same in both; {@code LayoutCopyRuntimeHints} puts them in the
 * image.
 * </p>
 */
public final class LayoutDefaults {

    /** The language of the base file, {@code messages.properties}. */
    private static final String BASE_LANGUAGE = "en";

    /** Every language the platform ships; the storefront picks the store's own out of the map. */
    private static final List<String> LANGUAGES = List.of(BASE_LANGUAGE, "ar", "es", "fr", "ru");

    private static final String BASE_COPY = "layout-defaults/messages.properties";

    private static final String LANGUAGE_COPY = "layout-defaults/messages_%s.properties";

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

    /** Each language's copy: the English base with the language's own file laid over it. */
    private static final Map<String, Properties> COPY = loadCopy();

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
    static Map<String, String> text(String key) {
        Map<String, String> byLanguage = new LinkedHashMap<>();
        for (String language : LANGUAGES) {
            byLanguage.put(language, Objects.requireNonNull(COPY.get(language).getProperty(key), key));
        }
        return byLanguage;
    }

    /** "en" is the base file itself, never the JVM's default locale; a missing language file leaves the base. */
    private static Map<String, Properties> loadCopy() {
        Properties base = read(BASE_COPY);
        Map<String, Properties> copy = new LinkedHashMap<>();
        for (String language : LANGUAGES) {
            Properties merged = new Properties();
            merged.putAll(base);
            if (!BASE_LANGUAGE.equals(language)) {
                merged.putAll(read(LANGUAGE_COPY.formatted(language)));
            }
            copy.put(language, merged);
        }
        return copy;
    }

    private static Properties read(String resource) {
        return read(LayoutDefaults.class.getClassLoader().getResourceAsStream(resource));
    }

    /**
     * UTF-8, as {@code PropertyResourceBundle} reads a bundle; {@code Properties.load(InputStream)} would not. An
     * absent file ({@code null}) is empty, so the English base stands.
     */
    @SneakyThrows(IOException.class)
    static Properties read(InputStream in) {
        Properties properties = new Properties();
        if (in != null) {
            try (in) {
                properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        }
        return properties;
    }

}
