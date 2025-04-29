package com.asrevo.cvhome.order.service.facade.country;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.reference.country.Country;
import com.asrevo.cvhome.order.service.populator.references.ReadableCountryPopulator;
import com.asrevo.cvhome.order.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CountryFacadeImpl implements CountryFacade {
    private CountryService countryService;

    @Override
    public List<ReadableCountry> getListCountryZones(
            LanguageCode language, StoreMerchantId merchantStore) {
        return getListOfCountryZones(language).stream()
                .map(country -> convertToReadableCountry(country, language, merchantStore))
                .collect(Collectors.toList());
    }

    private ReadableCountry convertToReadableCountry(
            Country country, LanguageCode language, StoreMerchantId merchantStore) {
        ReadableCountryPopulator populator = new ReadableCountryPopulator();
        return populator.populate(country, new ReadableCountry(), merchantStore, language);
    }

    private List<Country> getListOfCountryZones(LanguageCode language) {
        return countryService.listCountryZones(language);
    }
}
