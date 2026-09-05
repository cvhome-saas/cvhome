package com.asrevo.cvhome.commons.domain;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record LanguageCode(String code) implements Serializable, Comparable<LanguageCode> {
    public static LanguageCode defaultLanguage() {
        return new LanguageCode("en");
    }

    public static LanguageCode nonLanguage() {
        return new LanguageCode("_non");
    }

    public static LanguageCode allLanguage() {
        return new LanguageCode("_all");
    }

    public static boolean isNonLanguage(LanguageCode languageCode) {
        return nonLanguage().equals(languageCode);
    }

    public static boolean isAllLanguage(LanguageCode languageCode) {
        return allLanguage().equals(languageCode);
    }

    public static boolean isLanguage(LanguageCode code) {
        if (Objects.isNull(code)) {
            return false;
        }
        return code.isLanguage();
    }

    @Override
    public int compareTo(LanguageCode o) {
        return this.code.compareTo(o.code);
    }

    /**
     * An ISO-ish two- or three-letter code, and neither sentinel.
     *
     * <p>
     * The sentinels need no test of their own here: {@code _non} and {@code _all} are four characters, so the length
     * bound has already rejected them. This used to compare against both explicitly after the bound, which was dead
     * code — the arms could not be reached, and so could not be covered either.
     * </p>
     */
    @JsonIgnore
    public boolean isLanguage() {
        if (Objects.isNull(code)) {
            return false;
        }
        int length = code.trim().length();
        return length >= 2 && length <= 3;
    }
}
