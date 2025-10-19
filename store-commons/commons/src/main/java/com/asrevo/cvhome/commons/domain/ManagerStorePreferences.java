package com.asrevo.cvhome.commons.domain;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ManagerStorePreferences(
        Theme theme, LanguageCode defaultLanguage, List<LanguageCode> supportedLanguages) {
    public ManagerStorePreferences {

        if (theme == null) {
            throw new IllegalArgumentException("Theme cannot be null");
        }

        if (defaultLanguage == null) {
            throw new IllegalArgumentException("Default language cannot be null");
        }

        if (supportedLanguages == null || supportedLanguages.isEmpty()) {
            throw new IllegalStateException("supportedLanguages is null or empty");
        }
    }

    public static ManagerStorePreferences of(Map<Object, Object> request) {

        Theme theme =
                Optional.ofNullable(request.get("theme"))
                        .map(it -> Theme.valueOf(it.toString()))
                        .orElseThrow(() -> new IllegalArgumentException("Theme cannot be null"));

        LanguageCode defaultLanguage =
                Optional.ofNullable(request.get("defaultLanguage"))
                        .map(it -> new LanguageCode(it.toString()))
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Default language cannot be null"));

        List<LanguageCode> supportedLanguages =
                Optional.ofNullable(request.get("supportedLanguages"))
                        .map(it -> ((List<String>) it))
                        .filter(it -> !it.isEmpty())
                        .map(it -> it.stream().map(LanguageCode::new).toList())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "supportedLanguages cannot be null"));

        return new ManagerStorePreferences(theme, defaultLanguage, supportedLanguages);
    }
}
