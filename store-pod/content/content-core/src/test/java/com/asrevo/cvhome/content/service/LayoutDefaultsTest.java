package com.asrevo.cvhome.content.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.model.layout.LayoutSection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A new store's starter home carries its copy in every platform language. The copy is read as classpath resources so
 * the native image finds it: the first native load test could not ("Can't find bundle for base name
 * layout-defaults.messages").
 */
class LayoutDefaultsTest {

    private static final String HERO_SECTION = "sec-start-hero";

    private static final String HEADING = "heading";

    private static final String ARABIC = "ar";

    private static final String RUSSIAN = "ru";

    private static final String NO_SUCH_KEY = "no.such.key";

    private static Map<String, String> heroHeading() {
        LayoutSection hero = LayoutDefaults.starterHome().sections().stream()
                .filter(section -> HERO_SECTION.equals(section.id()))
                .findFirst()
                .orElseThrow();
        return hero.text().get(HEADING);
    }

    @Test
    void everyPlatformLanguageGetsItsOwnCopy() {
        Map<String, String> heading = heroHeading();

        assertThat(heading).containsOnlyKeys("en", ARABIC, "es", "fr", RUSSIAN);
        assertThat(heading.values()).doesNotContainNull().doesNotHaveDuplicates();
    }

    @Test
    void theCopyIsReadAsUtf8() {
        Map<String, String> heading = heroHeading();

        assertThat(heading.get(ARABIC)).containsPattern("\\p{IsArabic}");
        assertThat(heading.get(RUSSIAN)).containsPattern("\\p{IsCyrillic}");
    }

    @Test
    void aKeyWithoutCopyFailsLoudly() {
        assertThatThrownBy(() -> LayoutDefaults.text(NO_SUCH_KEY)).isInstanceOf(NullPointerException.class)
                .hasMessageContaining(NO_SUCH_KEY);
    }

    @Test
    void anAbsentLanguageFileReadsAsEmptySoTheEnglishBaseStands() {
        assertThat(LayoutDefaults.read((InputStream) null)).isEmpty();
    }

    @Test
    void anUnreadableCopyFileIsAnError() {
        InputStream unreadable = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("unreadable");
            }
        };

        assertThatThrownBy(() -> LayoutDefaults.read(unreadable)).isInstanceOf(IOException.class);
    }

}
