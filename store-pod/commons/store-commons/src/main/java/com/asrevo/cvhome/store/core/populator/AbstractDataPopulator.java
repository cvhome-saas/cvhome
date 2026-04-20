package com.asrevo.cvhome.store.core.populator;

import java.util.Locale;

import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractDataPopulator<S, M, T> implements DataPopulator<S, M, T> {

    private Locale locale;

    @Override
    public T populate(S source, M store, LanguageCode language) throws ConversionException {
        return populate(source, createTarget(), store, language);
    }

    protected abstract T createTarget();

}
