package com.asrevo.cvhome.commons.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What counts as a language, and what the two sentinels mean.
 *
 * <p>
 * {@code _non} and {@code _all} are not languages: descriptions stored against them are the language-agnostic row
 * and the every-language row respectively, and a caller that treats either as a real language writes a description
 * nobody can read back. {@link LanguageCode#isLanguage()} is the gate, and every service's argument resolver leans
 * on it, so its boundaries are pinned here.
 * </p>
 */
class LanguageCodeTest {

    private static final String ENGLISH = "en";

    @Test
    void theDefaultLanguageIsEnglish() {
        assertThat(LanguageCode.defaultLanguage().code()).isEqualTo(ENGLISH);
        assertThat(LanguageCode.defaultLanguage().isLanguage()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "fr", "ara", "de"})
    void aTwoOrThreeLetterCodeIsALanguage(String code) {
        assertThat(new LanguageCode(code).isLanguage()).isTrue();
        assertThat(LanguageCode.isLanguage(new LanguageCode(code))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "e", "toolong", "_non", "_all"})
    void anythingOutsideTwoToThreeCharactersIsNot(String code) {
        assertThat(new LanguageCode(code).isLanguage()).isFalse();
    }

    @Test
    void aNullCodeIsNotALanguageAndDoesNotThrow() {
        assertThat(new LanguageCode(null).isLanguage()).isFalse();
    }

    @Test
    void aNullLanguageCodeIsNotALanguageEither() {
        assertThat(LanguageCode.isLanguage(null)).isFalse();
    }

    @Test
    void theTwoSentinelsAreRecognisedByTheirOwnPredicates() {
        assertThat(LanguageCode.isNonLanguage(LanguageCode.nonLanguage())).isTrue();
        assertThat(LanguageCode.isAllLanguage(LanguageCode.allLanguage())).isTrue();
        assertThat(LanguageCode.isNonLanguage(LanguageCode.allLanguage())).isFalse();
        assertThat(LanguageCode.isAllLanguage(LanguageCode.nonLanguage())).isFalse();
        assertThat(LanguageCode.isNonLanguage(LanguageCode.defaultLanguage())).isFalse();
        assertThat(LanguageCode.isAllLanguage(LanguageCode.defaultLanguage())).isFalse();
    }

    @Test
    void codesSortAlphabetically() {
        assertThat(new LanguageCode("ar").compareTo(new LanguageCode(ENGLISH))).isNegative();
        assertThat(new LanguageCode(ENGLISH).compareTo(new LanguageCode(ENGLISH))).isZero();
        assertThat(new LanguageCode("fr").compareTo(new LanguageCode(ENGLISH))).isPositive();
    }
}
