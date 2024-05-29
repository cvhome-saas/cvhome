package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;


@Slf4j
public class LocaleUtils {


    public static Locale getLocale(Language language) {

        return new Locale(language.getCode());

    }

    /**
     * Creates a Locale object for currency format only with country code
     * This method ignoes the language
     *
     */
    public static Locale getLocale(MerchantStore store) {

        Locale defaultLocale = Constants.DEFAULT_LOCALE;
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale l : locales) {
            try {
                if (l.toLanguageTag().equals(store.getDefaultLanguage().getCode())) {
                    defaultLocale = l;
                    break;
                }
            } catch (Exception e) {
                log.error("An error occured while getting ISO code for locale {}", l.toString());
            }
        }

        return defaultLocale;

    }


}
