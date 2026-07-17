package com.asrevo.cvhome.store.core.populator;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.exception.ConversionException;

/**
 * @author Umesh A
 */
public interface DataPopulator<S, M, T> {

    T populate(S source, T target, M store, LanguageCode language) throws ConversionException;

    T populate(S source, M store, LanguageCode language) throws ConversionException;

}
