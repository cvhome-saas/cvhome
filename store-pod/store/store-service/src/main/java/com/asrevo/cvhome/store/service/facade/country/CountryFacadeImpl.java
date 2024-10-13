package com.asrevo.cvhome.store.service.facade.country;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.references.ReadableCountry;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.service.populator.references.ReadableCountryPopulator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CountryFacadeImpl implements CountryFacade {

    private final CountryService countryService;

    public CountryFacadeImpl(CountryService countryService) {
        this.countryService = countryService;
    }

    @Override
    public List<ReadableCountry> getListCountryZones(
            Language language, MerchantStore merchantStore) {
        return getListOfCountryZones(language).stream()
                .map(country -> convertToReadableCountry(country, language, merchantStore))
                .collect(Collectors.toList());
    }

    private ReadableCountry convertToReadableCountry(
            Country country, Language language, MerchantStore merchantStore) {
        try {
            ReadableCountryPopulator populator = new ReadableCountryPopulator();
            return populator.populate(country, new ReadableCountry(), merchantStore, language);
        } catch (ConversionException e) {
            throw new ConversionRuntimeException(e);
        }
    }

    private List<Country> getListOfCountryZones(Language language) {
        try {
            return countryService.listCountryZones(language);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }
}
