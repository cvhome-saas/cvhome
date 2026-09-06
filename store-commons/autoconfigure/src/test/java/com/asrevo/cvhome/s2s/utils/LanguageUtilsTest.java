package com.asrevo.cvhome.s2s.utils;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@code lang} query parameter: absent means the default language, {@code _all} means every language (null).
 */
class LanguageUtilsTest {

    private static final String FRENCH = "fr";

    @Test
    void absentOrEmptyIsTheDefaultLanguage() {
        assertThat(LanguageUtils.getRESTLanguageCode(null)).isEqualTo(LanguageCode.defaultLanguage());
        assertThat(LanguageUtils.getRESTLanguageCode("")).isEqualTo(LanguageCode.defaultLanguage());
    }

    @Test
    void aCodeIsThatLanguageAndAllIsNull() {
        assertThat(LanguageUtils.getRESTLanguageCode(FRENCH)).isEqualTo(new LanguageCode(FRENCH));
        assertThat(LanguageUtils.getRESTLanguageCode("_all")).isNull();
    }

}
