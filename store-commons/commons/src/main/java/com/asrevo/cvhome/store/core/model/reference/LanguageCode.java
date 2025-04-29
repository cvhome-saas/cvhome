package com.asrevo.cvhome.store.core.model.reference;

import java.io.Serializable;

public record LanguageCode(String code) implements Serializable, Comparable<LanguageCode> {
    @Override
    public int compareTo(LanguageCode o) {
        return this.code.compareTo(o.code);
    }

    public static LanguageCode defaultLanguage() {
        return new LanguageCode(("en"));
    }
}
