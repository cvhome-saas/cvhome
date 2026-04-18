package com.asrevo.cvhome.store.core.populator;

import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

/**
 * @author Umesh A
 */
public interface DataPopulator<Source, Store, Target> {

    Target populate(Source source, Target target, Store store, LanguageCode language) throws ConversionException;

    Target populate(Source source, Store store, LanguageCode language) throws ConversionException;

}
