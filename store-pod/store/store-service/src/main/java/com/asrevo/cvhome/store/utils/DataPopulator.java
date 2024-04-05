/**
 *
 */
package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;

/**
 * @author Umesh A
 */
public interface DataPopulator<Source, Target> {

    Target populate(Source source, Target target, MerchantStore store, Language language) throws ConversionException;

    Target populate(Source source, MerchantStore store, Language language) throws ConversionException;

}
