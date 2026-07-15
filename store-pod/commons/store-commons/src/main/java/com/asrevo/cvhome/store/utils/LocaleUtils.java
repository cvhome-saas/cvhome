package com.asrevo.cvhome.store.utils;

import java.util.Locale;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocaleUtils {
    private static final Locale[] locales = Locale.getAvailableLocales();

    private LocaleUtils() {
    }


    public static Locale getLocale(LanguageCode language) {
        Locale defaultLocale = Constants.DEFAULT_LOCALE;
        for (Locale l : locales) {
            try {
                if (l.toLanguageTag().equals(language.code())) {
                    defaultLocale = l;
                    break;
                }
            } catch (Exception _) {
                log.error("An error occurred while getting ISO code for locale {}", l.toString());
            }
        }

        return defaultLocale;
    }

}
