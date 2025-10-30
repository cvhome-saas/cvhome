package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.apache.commons.lang3.StringUtils;

public class LanguageUtils {

	private static final String ALL_LANGUAGES = "_all";

	public static LanguageCode getRESTLanguageCode(String lang) {
		if (StringUtils.isBlank(lang)) {
			return LanguageCode.defaultLanguage();
		}
		if (!ALL_LANGUAGES.equals(lang)) {
			return new LanguageCode(lang);

		}
		else {
			return null;
		}
	}

}
