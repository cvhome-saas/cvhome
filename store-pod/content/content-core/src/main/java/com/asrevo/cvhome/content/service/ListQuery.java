package com.asrevo.cvhome.content.service;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.TranslationState;

/**
 * Filters of a console list: status, a locale (optionally with the state it must be in) and free text.
 */
public record ListQuery(ContentStatus status, LanguageCode locale, TranslationState state, String q) {

    public static ListQuery none() {
        return new ListQuery(null, null, null, null);
    }

}
