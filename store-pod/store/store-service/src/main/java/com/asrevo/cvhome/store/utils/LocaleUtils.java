package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;


public class LocaleUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleUtils.class);

    public static Locale getLocale(Language language) {

        return new Locale(language.getCode());

    }

    /**
     * Creates a Locale object for currency format only with country code
     * This method ignoes the language
     *
     * @param store
     * @return
     */
    public static Locale getLocale(MerchantStore store) {

        Locale defaultLocale = Constants.DEFAULT_LOCALE;
        Locale[] locales = Locale.getAvailableLocales();
        for (int i = 0; i < locales.length; i++) {
            Locale l = locales[i];
            try {
                if (l.toLanguageTag().equals(store.getDefaultLanguage().getCode())) {
                    defaultLocale = l;
                    break;
                }
            } catch (Exception e) {
                LOGGER.error("An error occured while getting ISO code for locale " + l.toString());
            }
        }

        return defaultLocale;

    }


}
