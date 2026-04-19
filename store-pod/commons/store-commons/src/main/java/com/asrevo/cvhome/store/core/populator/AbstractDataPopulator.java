package com.asrevo.cvhome.store.core.populator;

import java.util.Locale;

import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractDataPopulator<Source, Store, Target> implements DataPopulator<Source, Store, Target> {

    private Locale locale;

    @Override
    public Target populate(Source source, Store store, LanguageCode language) throws ConversionException {
        return populate(source, createTarget(), store, language);
    }

    protected abstract Target createTarget();

}
