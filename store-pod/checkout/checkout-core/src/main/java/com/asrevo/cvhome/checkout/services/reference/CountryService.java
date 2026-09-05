package com.asrevo.cvhome.checkout.services.reference;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;

/**
 * The ISO country list the address forms offer. Backed by the JDK, not a table: the set of countries is not a thing a
 * store configures.
 */
public interface CountryService {

    List<ReadableCountry> all(LanguageCode language);

    boolean isKnown(String isoCode);
}
