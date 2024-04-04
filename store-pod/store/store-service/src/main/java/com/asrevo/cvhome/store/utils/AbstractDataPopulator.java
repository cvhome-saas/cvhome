/**
 *
 */
package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import lombok.Getter;
import lombok.Setter;

import java.util.Locale;


/**
 * @author Umesh A
 */
@Setter
@Getter
public abstract class AbstractDataPopulator<Source, Target> implements DataPopulator<Source, Target> {


    private Locale locale;

    @Override
    public Target populate(Source source, MerchantStore store, Language language) throws ConversionException {
        return populate(source, createTarget(), store, language);
    }

    protected abstract Target createTarget();


}
