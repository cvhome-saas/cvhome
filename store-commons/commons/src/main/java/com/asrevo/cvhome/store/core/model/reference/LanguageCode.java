package com.asrevo.cvhome.store.core.model.reference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Objects;

public record LanguageCode(String code) implements Serializable, Comparable<LanguageCode> {
    @Override
    public int compareTo(LanguageCode o) {
        return this.code.compareTo(o.code);
    }

    public static LanguageCode defaultLanguage() {
        return new LanguageCode(("en"));
    }

    public static LanguageCode nonLanguage() {
        return new LanguageCode(("_non"));
    }

    public static LanguageCode allLanguage() {
        return new LanguageCode(("_all"));
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

    @JsonIgnore
    public boolean isLanguage() {
        if (Objects.isNull(code)) {
            return false;
        }
        if (code.trim().isEmpty()) {
            return false;
        }
        if (code.trim().length() < 2) {
            return false;
        }
        if (code.trim().length() > 3) {
            return false;
        }
        if (this.equals(nonLanguage())) {
            return false;
        }
        if (this.equals(allLanguage())) {
            return false;
        }
        return true;
    }
}
