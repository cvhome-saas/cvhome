package com.asrevo.cvhome.checkout.services.reference.country;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.entity.reference.country.Country;
import com.asrevo.cvhome.checkout.repositories.reference.country.CountryRepository;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

import lombok.extern.slf4j.Slf4j;

@Service("countryService")
@Slf4j
public class CountryServiceImpl extends SalesManagerEntityServiceImpl<CountryIsoCode, Country>
        implements CountryService {

    private final CountryRepository countryRepository;

    @Autowired
    public CountryServiceImpl(CountryRepository countryRepository) {
        super(countryRepository);
        this.countryRepository = countryRepository;
    }

    @Cacheable("countrByCode")
    public Country getByCode(CountryIsoCode code) {
        return countryRepository.findByIsoCode(code);
    }

    @Override
    @Cacheable("countriesMap")
    public Map<CountryIsoCode, Country> getCountriesMap(LanguageCode language) {

        List<Country> countries = getCountries(language);

        Map<CountryIsoCode, Country> returnMap = new LinkedHashMap<>();

        for (Country country : countries) {
            returnMap.put(country.getIsoCode(), country);
        }

        return returnMap;
    }

    @Override
    public List<Country> listCountryZones(LanguageCode language) {
        return countryRepository.listCountryZonesByLanguage(language);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Country> getCountries(LanguageCode language) {
        return countryRepository.listByLanguage(language);
    }

}
