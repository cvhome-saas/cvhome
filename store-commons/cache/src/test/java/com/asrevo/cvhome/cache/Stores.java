package com.asrevo.cvhome.cache;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/** The stores and languages the tests of this module share. */
public final class Stores {

    public static final StoreMerchantId A = new StoreMerchantId("65f023632bc46470c104b75f");

    public static final StoreMerchantId B = new StoreMerchantId("65f020632bc46470c104b76f");

    public static final LanguageCode EN = LanguageCode.defaultLanguage();

    public static final LanguageCode ES = new LanguageCode("es");

    private Stores() {
    }
}
