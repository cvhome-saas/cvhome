package com.asrevo.cvhome.store.core.populator;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.errors.ConversionException;

/**
 * @author Umesh A
 */
/*
 * The one place a category base is declared deliberately: a populator SPI generic over three type parameters has no
 * condition to name, and every implementation narrows to its own — CustomerPopulator to
 * UnsupportedCountryCodeException, un-migrated ones to the deprecated store.core.exception.ConversionException. That
 * narrowing is what callers compile against, so the base never reaches a call site.
 */
public interface DataPopulator<S, M, T> {

    T populate(S source, T target, M store, LanguageCode language) throws ConversionException;

    T populate(S source, M store, LanguageCode language) throws ConversionException;

}
