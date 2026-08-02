package com.asrevo.cvhome.s2s.utils;

import java.util.Objects;

import com.asrevo.cvhome.commons.domain.LanguageCode;

public final class LanguageUtils {

    public static final String LANG = "lang";

    private static final String ALL_LANGUAGES = "_all";

    private LanguageUtils() {
    }

    public static LanguageCode getRESTLanguageCode(String lang) {
        if (Objects.isNull(lang) || lang.isEmpty()) {
            return LanguageCode.defaultLanguage();
        }
        if (!ALL_LANGUAGES.equals(lang)) {
            return new LanguageCode(lang);

        } else {
            return null;
        }
    }

}
