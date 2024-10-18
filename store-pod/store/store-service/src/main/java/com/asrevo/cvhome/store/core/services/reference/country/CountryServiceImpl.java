package com.asrevo.cvhome.store.core.services.reference.country;

import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.country.CountryDescription;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.reference.country.CountryRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.asrevo.cvhome.store.core.utils.CacheUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("countryService")
@Slf4j
public class CountryServiceImpl extends SalesManagerEntityServiceImpl<Integer, Country>
        implements CountryService {

    private final CountryRepository countryRepository;

    private final CacheUtils cache;

    @Autowired
    public CountryServiceImpl(CountryRepository countryRepository, CacheUtils cache) {
        super(countryRepository);
        this.countryRepository = countryRepository;
        this.cache = cache;
    }

    @Cacheable("countrByCode")
    public Country getByCode(String code) throws ServiceException {
        return countryRepository.findByIsoCode(code);
    }

    @Override
    public void addCountryDescription(Country country, CountryDescription description)
            throws ServiceException {
        country.getDescriptions().add(description);
        description.setCountry(country);
        update(country);
    }

    @Override
    @Cacheable("countriesMap")
    public Map<String, Country> getCountriesMap(Language language) throws ServiceException {

        List<Country> countries = getCountries(language);

        Map<String, Country> returnMap = new LinkedHashMap<>();

        for (Country country : countries) {
            returnMap.put(country.getIsoCode(), country);
        }

        return returnMap;
    }

    @Override
    public List<Country> getCountries(final List<String> isoCodes, final Language language)
            throws ServiceException {
        List<Country> countryList = getCountries(language);
        List<Country> requestedCountryList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(countryList)) {
            for (Country c : countryList) {
                if (isoCodes.contains(c.getIsoCode())) {
                    requestedCountryList.add(c);
                }
            }
        }
        return requestedCountryList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Country> getCountries(Language language) throws ServiceException {

        List<Country> countries = null;
        try {

            countries = (List<Country>) cache.getFromCache("COUNTRIES_" + language.getCode());
            if (countries == null) {

                countries = countryRepository.listByLanguage(language.getId());

                // set names
                for (Country country : countries) {

                    CountryDescription description = country.getDescriptions().iterator().next();
                    country.setName(description.getName());
                }

                cache.putInCache(countries, "COUNTRIES_" + language.getCode());
            }

        } catch (Exception e) {
            log.error("getCountries()", e);
        }

        return countries;
    }

    @Override
    public List<Country> listCountryZones(Language language) throws ServiceException {
        try {
            return countryRepository.listCountryZonesByLanguage(language.getId());
        } catch (Exception e) {
            log.error("listCountryZones", e);
            throw new ServiceException(e);
        }
    }
}
