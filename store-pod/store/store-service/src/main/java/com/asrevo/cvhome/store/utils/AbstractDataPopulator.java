/**
 *
 */
package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;

import java.util.Locale;


/**
 * @author Umesh A
 */
public abstract class AbstractDataPopulator<Source, Target> implements DataPopulator<Source, Target> {


    private Locale locale;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Target populate(Source source, MerchantStore store, Language language) throws ConversionException {
        return populate(source, createTarget(), store, language);
    }

    protected abstract Target createTarget();


}
