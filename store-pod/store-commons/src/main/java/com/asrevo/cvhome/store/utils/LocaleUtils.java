package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocaleUtils {

	public static Locale getLocale(LanguageCode language) {
		Locale defaultLocale = Constants.DEFAULT_LOCALE;
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale l : locales) {
			try {
				if (l.toLanguageTag().equals(language.code())) {
					defaultLocale = l;
					break;
				}
			}
			catch (Exception e) {
				log.error("An error occurred while getting ISO code for locale {}", l.toString());
			}
		}

		return defaultLocale;
	}

}
